package com.sqltutor.model;

import java.util.List;

public class QueryResult {
    private boolean success;
    private String  message;
    private List<String>       columns;
    private List<List<String>> rows;
    private int     rowsAffected;
    private long    executionTimeMs;

    public QueryResult() {}

    public static QueryResult ofSelect(List<String> columns, List<List<String>> rows, long ms) {
        QueryResult r = new QueryResult();
        r.success = true; r.columns = columns; r.rows = rows; r.executionTimeMs = ms;
        r.message = rows.size() + " row(s) returned in " + ms + "ms";
        return r;
    }
    public static QueryResult ofUpdate(int affected, long ms) {
        QueryResult r = new QueryResult();
        r.success = true; r.rowsAffected = affected; r.executionTimeMs = ms;
        r.message = "Query OK — " + affected + " row(s) affected in " + ms + "ms";
        return r;
    }
    public static QueryResult ofError(String err) {
        QueryResult r = new QueryResult();
        r.success = false; r.message = err;
        return r;
    }

    public boolean isSuccess()                { return success; }
    public void    setSuccess(boolean s)      { this.success = s; }
    public String  getMessage()               { return message; }
    public void    setMessage(String m)       { this.message = m; }
    public List<String> getColumns()          { return columns; }
    public void setColumns(List<String> c)    { this.columns = c; }
    public List<List<String>> getRows()       { return rows; }
    public void setRows(List<List<String>> r) { this.rows = r; }
    public int  getRowsAffected()             { return rowsAffected; }
    public void setRowsAffected(int n)        { this.rowsAffected = n; }
    public long getExecutionTimeMs()          { return executionTimeMs; }
    public void setExecutionTimeMs(long ms)   { this.executionTimeMs = ms; }
}
