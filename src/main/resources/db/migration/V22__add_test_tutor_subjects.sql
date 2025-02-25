-- Link new test tutors with subjects
INSERT INTO tutor_subjects (tutor_id, subject_id, hourly_rate, description)
SELECT t.tutor_id, s.subject_id, 50.00, CONCAT('Experienced in teaching ', s.name)
FROM Tutors t
INNER JOIN Users u ON t.user_id = u.user_id
CROSS JOIN (
    SELECT subject_id, name 
    FROM Subjects 
    WHERE name IN ('Mathematics', 'Physics', 'Chemistry') 
    LIMIT 3
) s
WHERE u.email LIKE 'tutor2%@test.com';

-- Add varying rates for different subjects
UPDATE tutor_subjects ts
INNER JOIN Subjects s ON ts.subject_id = s.subject_id
INNER JOIN Tutors t ON ts.tutor_id = t.tutor_id
INNER JOIN Users u ON t.user_id = u.user_id
SET ts.hourly_rate = 
    CASE s.name
        WHEN 'Mathematics' THEN 50.00
        WHEN 'Physics' THEN 55.00
        WHEN 'Chemistry' THEN 45.00
    END
WHERE u.email LIKE 'tutor2%@test.com';