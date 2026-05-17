CREATE DATABASE IF NOT EXISTS nextexam_db;
USE nextexam_db;

CREATE TABLE IF NOT EXISTS users (
                                     user_id VARCHAR(30) PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Active',
    profile_image VARCHAR(255) DEFAULT ''
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
    question_type VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_answer VARCHAR(20),
    marks DECIMAL(8,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Draft',
    model_answer TEXT
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

USE nextexam_db;

DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
     notification_id VARCHAR(30) PRIMARY KEY,
     target_user_id VARCHAR(30) DEFAULT '',
     target_role VARCHAR(30) DEFAULT '',
     title VARCHAR(180) NOT NULL,
     message TEXT NOT NULL,
     type VARCHAR(30) NOT NULL,
     status VARCHAR(30) NOT NULL DEFAULT 'Unread',
     created_at DATETIME NOT NULL,
     read_at DATETIME NULL,
     target_url VARCHAR(255) DEFAULT ''
);
INSERT IGNORE INTO users (user_id, username, password, email, role, status, profile_image)
VALUES
    ('USR001', 'admin', 'admin123', 'admin@nextexam.lk', 'Admin', 'Active', ''),
    ('USR002', 'lecturer', 'lecturer123', 'lecturer@nextexam.lk', 'Lecturer', 'Active', ''),
    ('USR003', 'student', 'student123', 'student@nextexam.lk', 'Student', 'Active', '');

INSERT IGNORE INTO students (student_id, name, email, course, batch, contact, exam_status)
VALUES
    ('USR003', 'Student User', 'student@nextexam.lk', 'Software Engineering', 'Y1S2', '0770000000', 'Eligible'),
    ('ST001', 'Nimal Perera', 'nimal@nextexam.lk', 'Software Engineering', 'Y1S1', '0771111111', 'Eligible'),
    ('ST002', 'Kavindi Silva', 'kavindi@nextexam.lk', 'Information Technology', 'Y2S1', '0772222222', 'Pending'),
    ('ST003', 'Ruwan Fernando', 'ruwan@nextexam.lk', 'Computer Science', 'Y3S1', '0773333333', 'Blocked');


INSERT IGNORE INTO exams (exam_id, subject, exam_date, duration, total_marks, status)
VALUES
    ('EX001', 'Object Oriented Programming', '2026-05-25', 60, 100.00, 'Scheduled'),
    ('EX002', 'Data Structures and Algorithms', '2026-05-28', 90, 100.00, 'Active'),
    ('EX003', 'Database Management Systems', '2026-06-02', 120, 100.00, 'Draft');

INSERT IGNORE INTO exam_submissions
(submission_id, exam_id, student_id, student_name, submitted_at, answers_data, score, total_marks, status)
VALUES
    ('SUB001', 'EX001', 'USR003', 'Student User', '2026-05-17 20:30:00', 'Q001=B,flagged=NO;Q002=C,flagged=NO;Q003=Constructor overloading example,flagged=NO', 10.00, 20.00, 'Marked');

INSERT IGNORE INTO results
(result_id, student_id, exam_id, marks, grade, status, verification, published)
VALUES
    ('RES001', 'USR003', 'EX001', 75.00, 'A', 'Pass', 'Verified', 'Published'),
    ('RES002', 'ST001', 'EX001', 62.00, 'C', 'Pass', 'Pending', 'Not Published'),
    ('RES003', 'ST002', 'EX002', 35.00, 'F', 'Fail', 'Review', 'Not Published');

INSERT IGNORE INTO notifications
(notification_id, target_user_id, target_role, title, message, type, status, created_at, read_at, target_url)
VALUES
    ('NT001', 'USR003', '', 'Welcome to NextExamLK', 'Your student account is ready.', 'System', 'Unread', '2026-05-17 20:00:00', NULL, '/dashboard.jsp'),
    ('NT002', '', 'Admin', 'System Ready', 'MySQL migration is in progress.', 'System', 'Unread', '2026-05-17 20:05:00', NULL, '/dashboard.jsp'),
    ('NT003', '', 'All', 'Exam Portal Notice', 'Please check upcoming exams regularly.', 'Notice', 'Unread', '2026-05-17 20:10:00', NULL, '/my-exams');





















