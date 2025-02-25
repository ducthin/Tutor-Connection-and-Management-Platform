-- Insert test users
INSERT INTO Users (email, password, full_name, role, is_active, is_email_verified)
VALUES 
('tutor21@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 21', 'ROLE_TUTOR', true, true),
('tutor22@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 22', 'ROLE_TUTOR', true, true),
('tutor23@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 23', 'ROLE_TUTOR', true, true),
('tutor24@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 24', 'ROLE_TUTOR', true, true),
('tutor25@test.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor 25', 'ROLE_TUTOR', true, true);

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
FROM Users WHERE email LIKE 'tutor2%@test.com';