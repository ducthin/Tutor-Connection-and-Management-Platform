-- Get the demo tutor's ID
SET @tutor_id = (SELECT tutor_id FROM Tutors t 
                 INNER JOIN Users u ON t.user_id = u.user_id 
                 WHERE u.email = 'tutor@tcmp.com' LIMIT 1);

-- Add some test subjects if they don't exist already
INSERT INTO tutor_subjects (tutor_id, subject_id, hourly_rate, experience_years, description, is_active)
SELECT @tutor_id, s.subject_id, 50.00, 5, CONCAT('Expert in teaching ', s.name), true
FROM Subjects s
WHERE s.name IN ('Mathematics', 'Physics', 'Programming')
AND NOT EXISTS (
    SELECT 1 FROM tutor_subjects ts 
    WHERE ts.tutor_id = @tutor_id AND ts.subject_id = s.subject_id
);