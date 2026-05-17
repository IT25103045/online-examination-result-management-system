CREATE DATABASE IF NOT EXISTS nextexam_db;
USE nextexam_db;

CREATE TABLE IF NOT EXISTS users (
                                     user_id VARCHAR(30) PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    display_name VARCHAR(120),
    email VARCHAR(120),
    status VARCHAR(30) DEFAULT 'Active',
    profile_image VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS students (
                                        student_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    course VARCHAR(120) NOT NULL,
    batch VARCHAR(30) NOT NULL,
    contact VARCHAR(30) NOT NULL,
    exam_status VARCHAR(30) NOT NULL
    );

CREATE TABLE IF NOT EXISTS exams (
                                     exam_id VARCHAR(30) PRIMARY KEY,
    subject VARCHAR(150) NOT NULL,
    exam_date DATE NOT NULL,
    duration INT NOT NULL,
    total_marks DECIMAL(8,2) NOT NULL,
    status VARCHAR(30) NOT NULL
    );

CREATE TABLE IF NOT EXISTS questions (
                                         question_id VARCHAR(30) PRIMARY KEY,
    exam_id VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_answer VARCHAR(20),
    marks DECIMAL(8,2) NOT NULL,
    status VARCHAR(30) DEFAULT 'Active'
    );

CREATE TABLE IF NOT EXISTS exam_submissions (
                                                submission_id VARCHAR(30) PRIMARY KEY,
    exam_id VARCHAR(30) NOT NULL,
    student_id VARCHAR(30) NOT NULL,
    student_name VARCHAR(120) NOT NULL,
    submitted_at DATETIME NOT NULL,
    answers_data LONGTEXT,
    score DECIMAL(8,2),
    total_marks DECIMAL(8,2),
    status VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS results (
                                       result_id VARCHAR(30) PRIMARY KEY,
    student_id VARCHAR(30) NOT NULL,
    exam_id VARCHAR(30) NOT NULL,
    marks DECIMAL(8,2) NOT NULL,
    grade VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL,
    verification VARCHAR(30) NOT NULL,
    published VARCHAR(30) NOT NULL
    );