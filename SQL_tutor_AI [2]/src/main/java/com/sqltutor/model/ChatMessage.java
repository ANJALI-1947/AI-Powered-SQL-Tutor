package com.sqltutor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "sql_context", columnDefinition = "TEXT")
    private String sqlContext;

    public ChatMessage() {}

    public ChatMessage(String username, String role, String message, String sqlContext) {
        this.username   = username;
        this.role       = role;
        this.message    = message;
        this.timestamp  = LocalDateTime.now();
        this.sqlContext = sqlContext;
    }

    public Long getId()              { return id; }
    public String getUsername()      { return username; }
    public String getRole()          { return role; }
    public String getMessage()       { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSqlContext()    { return sqlContext; }

    public void setId(Long id)                       { this.id = id; }
    public void setUsername(String username)         { this.username = username; }
    public void setRole(String role)                 { this.role = role; }
    public void setMessage(String message)           { this.message = message; }
    public void setTimestamp(LocalDateTime timestamp){ this.timestamp = timestamp; }
    public void setSqlContext(String sqlContext)     { this.sqlContext = sqlContext; }

    public String getFormattedTime() {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}