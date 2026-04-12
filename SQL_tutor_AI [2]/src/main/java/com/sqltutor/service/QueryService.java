package com.sqltutor.service;

import com.sqltutor.model.QueryHistory;
import com.sqltutor.model.QueryResult;
import com.sqltutor.repository.QueryHistoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class QueryService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private QueryHistoryRepository historyRepo;

    // ✅ FIX: Removed DROP,CREATE from blocked since INSERT/UPDATE are OK;
    //         kept dangerous DDL blocked
    private static final String[] BLOCKED = {
        "DROP", "TRUNCATE", "ALTER", "CREATE", "GRANT", "REVOKE", "FLUSH"
    };

    public QueryResult executeQuery(String sql, String username, HttpSession session) {
        if (sql == null || sql.isBlank())
            return QueryResult.ofError("No SQL provided.");

        // ✅ FIX: Reject MySQL-specific USE command with helpful message
        if (sql.toUpperCase().trim().startsWith("USE "))
            return QueryResult.ofError("USE is not needed — the database is set in application.properties.");

        // ✅ FIX: Reject MySQL-specific DESCRIBE, suggest PostgreSQL alternative
        String upperTrimmed = sql.toUpperCase().trim();
        if (upperTrimmed.startsWith("DESCRIBE ")) {
            String tableName = sql.trim().substring(9).trim().replaceAll(";", "");
            String pgDescribe = "SELECT column_name, data_type, character_maximum_length, is_nullable " +
                                "FROM information_schema.columns " +
                                "WHERE table_name = '" + tableName.toLowerCase() + "' " +
                                "ORDER BY ordinal_position;";
            return executeQuery(pgDescribe, username, session);
        }

        String blocked = findBlocked(sql);
        if (blocked != null)
            return QueryResult.ofError("⛔ '" + blocked + "' is not allowed in this tutor.");

        long    start  = System.currentTimeMillis();
        boolean isSel  = upperTrimmed.startsWith("SELECT") || upperTrimmed.startsWith("SHOW")
                      || upperTrimmed.startsWith("EXPLAIN") || upperTrimmed.startsWith("WITH");
        QueryResult result;

        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {
            stmt.setMaxRows(500);
            if (isSel) {
                ResultSet rs           = stmt.executeQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int n = meta.getColumnCount();
                List<String> cols = new ArrayList<>();
                for (int i = 1; i <= n; i++) cols.add(meta.getColumnLabel(i));
                List<List<String>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= n; i++) {
                        String v = rs.getString(i);
                        row.add(v != null ? v : "NULL");
                    }
                    rows.add(row);
                }
                result = QueryResult.ofSelect(cols, rows, System.currentTimeMillis() - start);
            } else {
                int affected = stmt.executeUpdate(sql);
                result = QueryResult.ofUpdate(affected, System.currentTimeMillis() - start);
            }
        } catch (SQLException e) {
            long ms = System.currentTimeMillis() - start;
            saveToDb(username, sql, false, 0, ms, e.getMessage());
            return QueryResult.ofError("SQL Error: " + e.getMessage());
        }

        int rows = result.getRows() != null ? result.getRows().size() : result.getRowsAffected();
        saveToDb(username, sql, true, rows, result.getExecutionTimeMs(), null);
        saveToSession(session, sql);
        return result;
    }

    private void saveToDb(String u, String sql, boolean ok, int rows, long ms, String err) {
        try {
            historyRepo.save(new QueryHistory(u, sql, ok, rows, ms, err));
        } catch (Exception e) {
            System.err.println("[QueryService] DB save error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void saveToSession(HttpSession session, String sql) {
        if (session == null) return;
        List<String> h = (List<String>) session.getAttribute("sessionHistory");
        if (h == null) { h = new ArrayList<>(); session.setAttribute("sessionHistory", h); }
        h.remove(sql);
        h.add(0, sql);
        if (h.size() > 20) h.remove(h.size() - 1);
    }

    public List<QueryHistory> getDbHistory(String username) {
        return historyRepo.findRecentByUsername(username, PageRequest.of(0, 30));
    }

    @SuppressWarnings("unchecked")
    public List<String> getSessionHistory(HttpSession session) {
        if (session == null) return new ArrayList<>();
        List<String> h = (List<String>) session.getAttribute("sessionHistory");
        return h != null ? h : new ArrayList<>();
    }

    public long getQueryCount(String username) { return historyRepo.countByUsername(username); }

    @Transactional
    public void clearHistory(String username) { historyRepo.deleteByUsername(username); }

    /**
     * ✅ FIX: Use information_schema instead of DatabaseMetaData.getTables()
     *          which can be unreliable across PostgreSQL JDBC versions.
     *          Filters to only user-created tables in the 'public' schema.
     */
    public List<TableInfo> getSchema() {
        List<TableInfo> tables = new ArrayList<>();
        String tableQuery =
            "SELECT table_name FROM information_schema.tables " +
            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
            "ORDER BY table_name";
        String colQuery =
            "SELECT column_name, data_type FROM information_schema.columns " +
            "WHERE table_schema = 'public' AND table_name = ? " +
            "ORDER BY ordinal_position";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement tableStmt = conn.prepareStatement(tableQuery);
             ResultSet tableRs = tableStmt.executeQuery()) {

            while (tableRs.next()) {
                String tableName = tableRs.getString("table_name");
                TableInfo ti = new TableInfo(tableName);

                try (PreparedStatement colStmt = conn.prepareStatement(colQuery)) {
                    colStmt.setString(1, tableName);
                    ResultSet colRs = colStmt.executeQuery();
                    while (colRs.next()) {
                        ti.getColumns().add(
                            colRs.getString("column_name") +
                            " (" + colRs.getString("data_type") + ")"
                        );
                    }
                }
                tables.add(ti);
            }
        } catch (SQLException e) {
            System.err.println("[QueryService] Schema fetch error: " + e.getMessage());
        }
        return tables;
    }

    private String findBlocked(String sql) {
        String u = sql.toUpperCase();
        for (String kw : BLOCKED) if (u.contains(kw)) return kw;
        return null;
    }

    public static class TableInfo {
        private final String tableName;
        private final List<String> columns = new ArrayList<>();
        public TableInfo(String n)       { tableName = n; }
        public String getTableName()     { return tableName; }
        public List<String> getColumns() { return columns; }
    }
}
