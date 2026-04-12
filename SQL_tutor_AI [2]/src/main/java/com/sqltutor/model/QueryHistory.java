package com.sqltutor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "query_history")
public class QueryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "sql_query", nullable = false, columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "success")
    private boolean success;

    @Column(name = "rows_returned")
    private Integer rowsReturned;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public QueryHistory() {}

    public QueryHistory(String username, String sqlQuery,
                        boolean success, Integer rowsReturned,
                        Long executionTimeMs, String errorMessage) {
        this.username        = username;
        this.sqlQuery        = sqlQuery;
        this.executedAt      = LocalDateTime.now();
        this.success         = success;
        this.rowsReturned    = rowsReturned;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage    = errorMessage;
    }

    public Long getId()                  { return id; }
    public String getUsername()          { return username; }
    public String getSqlQuery()          { return sqlQuery; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public boolean isSuccess()           { return success; }
    public Integer getRowsReturned()     { return rowsReturned; }
    public Long getExecutionTimeMs()     { return executionTimeMs; }
    public String getErrorMessage()      { return errorMessage; }

    public void setId(Long id)                          { this.id = id; }
    public void setUsername(String username)            { this.username = username; }
    public void setSqlQuery(String sqlQuery)            { this.sqlQuery = sqlQuery; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    public void setSuccess(boolean success)             { this.success = success; }
    public void setRowsReturned(Integer rowsReturned)   { this.rowsReturned = rowsReturned; }
    public void setExecutionTimeMs(Long executionTimeMs){ this.executionTimeMs = executionTimeMs; }
    public void setErrorMessage(String errorMessage)    { this.errorMessage = errorMessage; }

    public String getFormattedTime() {
        if (executedAt == null) return "-";
        return executedAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
}