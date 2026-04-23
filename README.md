# SQL Tutor and Debugger with AI Assistance

## Project Description

SQL Tutor and Debugger with AI Assistance is a web-based learning platform designed to help users learn, practice, and debug SQL queries in a real-time database environment. The system provides an interactive interface where users can execute SQL queries and instantly view results, making SQL learning more practical and efficient.

The project is built using Spring Boot, Hibernate (JPA), MySQL, and Servlets, ensuring robust backend processing and smooth database interaction. It also integrates Spring Security for secure authentication and session management.

A key highlight of this project is the integration of Artificial Intelligence using the Groq API, which enhances the learning experience by:
- Suggesting SQL queries based on user input
- Explaining query logic in simple terms
- Assisting in debugging SQL errors

This makes the platform highly beginner-friendly and interactive for learners.

---

## Features

- Real-time SQL query execution
- Database schema viewer
- Query history tracking (session-based and persistent)
- Secure authentication using Spring Security
- AI-powered SQL assistance (Groq API integration)
- Intelligent error debugging and suggestions
- User-friendly interface for learning SQL

---

## Tech Stack

### Backend
- Java
- Spring Boot
- Hibernate (JPA)
- Servlets
- Spring Security

### Database
- MySQL

### AI Integration
- Groq API (LLM-based assistance)

### Frontend
- HTML
- CSS
- JavaScript

---

## AI Integration

The system uses Groq API to enhance SQL learning by:
- Generating optimized SQL queries
- Explaining query execution steps
- Identifying and fixing errors in user queries
- Providing learning support for beginners

---

## Project Structure (MVC Architecture)

src/main/java

- controller        Handles HTTP requests (Servlets / Controllers)
- service           Business logic layer
- repository        Data access layer (JPA/Hibernate)
- model             Entity classes
- config            Security and configuration files

---

## Security Features

- User authentication using Spring Security
- Session management
- Protected API endpoints

---

## Future Enhancements

- Support for multiple SQL databases (PostgreSQL, Oracle)
- Advanced AI-based query optimization
- Collaborative SQL learning mode
- SQL auto-completion feature
- Query performance analysis

---

## Conclusion

SQL Tutor and Debugger with AI Assistance combines traditional database learning with modern AI capabilities to create an intelligent and interactive SQL learning platform.

---

## Author

- Developed by: Your Name
- GitHub: Your GitHub Link
