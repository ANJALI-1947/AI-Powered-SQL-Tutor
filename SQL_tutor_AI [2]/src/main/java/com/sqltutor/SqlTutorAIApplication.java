package com.sqltutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SqlTutorAIApplication — Spring Boot Entry Point
 *
 * Features:
 *  ✅ Spring MVC (Controller layer — replaces raw Servlets)
 *  ✅ Spring Data JPA + Hibernate (ORM)
 *  ✅ Spring Security (Login + Session Management)
 *  ✅ JavaFX Desktop Client (connects to this Spring Boot server)
 *  ✅ AI Chatbot (Deep Learning via Hugging Face API)
 */
@SpringBootApplication
public class SqlTutorAIApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqlTutorAIApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  SQL Tutor AI is running!");
        System.out.println("  Web UI  → http://localhost:8080");
        System.out.println("  JavaFX  → run SQLTutorFXApp.java");
        System.out.println("========================================\n");
    }
}
