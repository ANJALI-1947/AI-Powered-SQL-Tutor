-- ============================================================
-- SQL Tutor - Sample Database Schema (PostgreSQL Compatible)
-- Run this in PostgreSQL to set up the sample database.
-- Tables for Spring Data JPA are auto-created by Hibernate.
-- ============================================================

-- ---- Table: students ----
CREATE TABLE IF NOT EXISTS students (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    age           INTEGER,
    email         VARCHAR(150) UNIQUE,
    department    VARCHAR(50),
    gpa           NUMERIC(3,2),
    enrolled_year INTEGER
);

-- ---- Table: courses ----
CREATE TABLE IF NOT EXISTS courses (
    id          SERIAL PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20)  UNIQUE,
    credits     INTEGER,
    department  VARCHAR(50),
    instructor  VARCHAR(100)
);

-- ---- Table: enrollments ----
CREATE TABLE IF NOT EXISTS enrollments (
    id         SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id),
    course_id  INTEGER REFERENCES courses(id),
    grade      VARCHAR(2),
    semester   VARCHAR(20)
);

-- ---- Table: departments ----
CREATE TABLE IF NOT EXISTS departments (
    id           SERIAL PRIMARY KEY,
    dept_name    VARCHAR(100),
    head_of_dept VARCHAR(100),
    building     VARCHAR(50),
    budget       NUMERIC(12,2)
);

-- ============================================================
-- Sample Data (use INSERT ... ON CONFLICT DO NOTHING to be safe)
-- ============================================================

INSERT INTO departments (dept_name, head_of_dept, building, budget) VALUES
('Computer Science', 'Dr. Ramesh Kumar', 'Tech Block A',  5000000.00),
('Mathematics',      'Dr. Priya Nair',   'Science Block', 3000000.00),
('Physics',          'Dr. Arun Mehta',   'Science Block', 2500000.00),
('Electronics',      'Dr. Sita Raman',   'Eng Block B',   4000000.00)
ON CONFLICT DO NOTHING;

INSERT INTO students (name, age, email, department, gpa, enrolled_year) VALUES
('Arjun Sharma',   20, 'arjun@college.edu',   'Computer Science', 3.85, 2022),
('Priya Menon',    21, 'priya@college.edu',    'Mathematics',      3.72, 2021),
('Kiran Raj',      19, 'kiran@college.edu',    'Computer Science', 3.50, 2023),
('Deepa Nair',     22, 'deepa@college.edu',    'Physics',          3.90, 2020),
('Rohit Verma',    20, 'rohit@college.edu',    'Electronics',      3.45, 2022),
('Ananya Iyer',    21, 'ananya@college.edu',   'Computer Science', 3.95, 2021),
('Vijay Kumar',    23, 'vijay@college.edu',    'Mathematics',      3.30, 2019),
('Sneha Pillai',   20, 'sneha@college.edu',    'Electronics',      3.60, 2022),
('Aditya Singh',   19, 'aditya@college.edu',   'Computer Science', 3.75, 2023),
('Meera Krishnan', 22, 'meera@college.edu',    'Physics',          3.88, 2020)
ON CONFLICT (email) DO NOTHING;

INSERT INTO courses (course_name, course_code, credits, department, instructor) VALUES
('Data Structures',     'CS201', 4, 'Computer Science', 'Dr. Ramesh Kumar'),
('Database Management', 'CS301', 3, 'Computer Science', 'Dr. Lakshmi Rao'),
('Linear Algebra',      'MA101', 3, 'Mathematics',      'Dr. Priya Nair'),
('Quantum Mechanics',   'PH201', 4, 'Physics',          'Dr. Arun Mehta'),
('Digital Electronics', 'EC101', 3, 'Electronics',      'Dr. Sita Raman'),
('Operating Systems',   'CS401', 4, 'Computer Science', 'Dr. Suresh Babu'),
('Calculus',            'MA102', 3, 'Mathematics',      'Dr. Anjali Menon'),
('Circuit Theory',      'EC201', 3, 'Electronics',      'Dr. Sita Raman'),
('Machine Learning',    'CS501', 3, 'Computer Science', 'Dr. Ramesh Kumar'),
('Classical Mechanics', 'PH101', 3, 'Physics',          'Dr. Arun Mehta')
ON CONFLICT (course_code) DO NOTHING;

INSERT INTO enrollments (student_id, course_id, grade, semester) VALUES
(1, 1, 'A',  'Fall 2022'),
(1, 2, 'A-', 'Spring 2023'),
(1, 9, 'B+', 'Fall 2023'),
(2, 3, 'A',  'Fall 2021'),
(2, 7, 'A-', 'Spring 2022'),
(3, 1, 'B+', 'Fall 2023'),
(3, 5, 'A',  'Fall 2023'),
(4, 4, 'A',  'Spring 2021'),
(4,10, 'A-', 'Fall 2020'),
(5, 5, 'B',  'Fall 2022'),
(5, 8, 'B+', 'Spring 2023'),
(6, 1, 'A',  'Fall 2021'),
(6, 2, 'A',  'Spring 2022'),
(6, 9, 'A',  'Fall 2022'),
(7, 3, 'B-', 'Spring 2020'),
(8, 5, 'A-', 'Fall 2022'),
(8, 8, 'B+', 'Spring 2023'),
(9, 1, 'A-', 'Fall 2023'),
(10,4, 'A',  'Fall 2020'),
(10,10,'A',  'Spring 2021');
