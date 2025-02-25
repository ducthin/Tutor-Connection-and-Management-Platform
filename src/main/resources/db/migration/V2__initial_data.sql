-- Reset auto-increment values
ALTER TABLE Users AUTO_INCREMENT = 1;
ALTER TABLE Students AUTO_INCREMENT = 1;
ALTER TABLE Tutors AUTO_INCREMENT = 1;
ALTER TABLE verification_tokens AUTO_INCREMENT = 1;

-- Insert admin user
INSERT INTO Users (email, password, full_name, role, is_active, is_email_verified, created_at)
VALUES (
    'admin@tcmp.com',
    '$2a$10$FvHQqZRtIGsXvUCQn39XmeqyX56WeuIJhgGU0v5tf74ICcSoOPqYi',  -- password: admin123
    'System Admin',
    'ROLE_ADMIN',
    true,
    true,
    CURRENT_TIMESTAMP
);

-- Insert demo tutor
INSERT INTO Users (email, password, full_name, role, is_active, is_email_verified, bio, created_at)
VALUES (
    'tutor@tcmp.com',
    '$2a$10$FvHQqZRtIGsXvUCQn39XmeqyX56WeuIJhgGU0v5tf74ICcSoOPqYi',  -- password: admin123
    'Demo Tutor',
    'ROLE_TUTOR',
    true,
    true,
    'Experienced tutor with a passion for teaching',
    CURRENT_TIMESTAMP
);

INSERT INTO Tutors (user_id, qualifications, subjects_taught, hourly_rate, is_verified, bio, rating)
VALUES (
    (SELECT user_id FROM Users WHERE email = 'tutor@tcmp.com'),
    'M.Sc. in Mathematics, 5+ years teaching experience',
    'Mathematics, Physics, Computer Science',
    50.00,
    true,
    'Specialized in helping students understand complex concepts through practical examples',
    5.00
);

-- Insert demo student
INSERT INTO Users (email, password, full_name, role, is_active, is_email_verified, created_at)
VALUES (
    'student@tcmp.com',
    '$2a$10$FvHQqZRtIGsXvUCQn39XmeqyX56WeuIJhgGU0v5tf74ICcSoOPqYi',  -- password: admin123
    'Demo Student',
    'ROLE_STUDENT',
    true,
    true,
    CURRENT_TIMESTAMP
);

INSERT INTO Students (user_id, learning_preferences, subjects_of_interest)
VALUES (
    (SELECT user_id FROM Users WHERE email = 'student@tcmp.com'),
    'Visual learning, interactive sessions',
    'Mathematics, Physics'
);

-- Insert basic subjects
INSERT INTO Subjects (name, category, description) VALUES 
('Mathematics', 'Science', 'Mathematics including algebra, calculus, and geometry'),
('Physics', 'Science', 'Physics fundamentals and advanced concepts'),
('Chemistry', 'Science', 'Chemistry including organic and inorganic'),
('Biology', 'Science', 'Biology fundamentals and advanced topics'),
('English', 'Languages', 'English language and literature'),
('Programming', 'Technology', 'Computer programming and software development'),
('History', 'Humanities', 'World history and historical analysis'),
('Geography', 'Humanities', 'Physical and human geography');

-- Link demo tutor with subjects
INSERT INTO tutor_subjects (tutor_id, subject_id, hourly_rate, description)
SELECT 
    (SELECT tutor_id FROM Tutors WHERE user_id = (SELECT user_id FROM Users WHERE email = 'tutor@tcmp.com')),
    subject_id,
    CASE 
        WHEN name IN ('Mathematics', 'Physics') THEN 50.00
        ELSE 45.00
    END,
    CONCAT('Experienced in teaching ', name)
FROM Subjects
WHERE name IN ('Mathematics', 'Physics', 'Programming');