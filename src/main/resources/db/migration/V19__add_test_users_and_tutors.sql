-- Insert test users
INSERT INTO Users (email, password, full_name, role, is_active, is_email_verified)
VALUES 
('tutor1@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 1', 'ROLE_TUTOR', true, true),
('tutor2@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 2', 'ROLE_TUTOR', true, true),
('tutor3@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 3', 'ROLE_TUTOR', true, true),
('tutor4@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 4', 'ROLE_TUTOR', true, true),
('tutor5@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 5', 'ROLE_TUTOR', true, true);

-- Insert test tutors
INSERT INTO Tutors (user_id, qualifications, subjects_taught, hourly_rate, is_verified, bio, rating)
SELECT 
    user_id,
    'Master\'s Degree in Education',
    'Mathematics, Physics',
    50.00,
    true,
    'Experienced tutor with proven track record',
    4.5
FROM Users 
WHERE email LIKE 'tutor%@test.com'
ORDER BY user_id ASC;

-- Link tutors with subjects
INSERT IGNORE INTO tutor_subjects (tutor_id, subject_id, hourly_rate, description)
SELECT 
    t.tutor_id,
    s.subject_id,
    t.hourly_rate,
    CONCAT('Experienced in teaching ', s.name)
FROM Tutors t
CROSS JOIN Subjects s
WHERE t.tutor_id <= 5
ORDER BY t.tutor_id, s.subject_id
LIMIT 10;