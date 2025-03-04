-- Insert test users first
INSERT INTO users (email, password, full_name, role, is_active, is_email_verified)
SELECT 'test.tutor1@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.268MC7.JCIfxJ6AJt2J2I3wgglPBkvS', 'Test Tutor One', 'ROLE_TUTOR', true, true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'test.tutor1@example.com');

-- Get the user_id for the tutor
INSERT INTO tutors (user_id, qualifications, subjects_taught, hourly_rate, is_verified, bio, rating)
SELECT u.user_id, 'Master''s in Education', 'Mathematics, Physics', 50.00, true, 'Experienced tutor with 5+ years of teaching.', 4.5
FROM users u 
WHERE u.email = 'test.tutor1@example.com'
AND NOT EXISTS (SELECT 1 FROM tutors t WHERE t.user_id = u.user_id);

-- Insert a test subject
INSERT INTO subjects (name, description, is_active)
SELECT 'Mathematics', 'Advanced Mathematics including Calculus and Algebra', true
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Mathematics');

-- Link tutor with subject
INSERT INTO tutor_subjects (tutor_id, subject_id, hourly_rate, description, is_active, experience_years)
SELECT t.tutor_id, s.subject_id, 50.00, 'Experienced in teaching advanced mathematics', true, 5
FROM tutors t
INNER JOIN users u ON t.user_id = u.user_id
INNER JOIN subjects s ON s.name = 'Mathematics'
WHERE u.email = 'test.tutor1@example.com'
AND NOT EXISTS (
    SELECT 1 
    FROM tutor_subjects ts 
    WHERE ts.tutor_id = t.tutor_id 
    AND ts.subject_id = s.subject_id
);