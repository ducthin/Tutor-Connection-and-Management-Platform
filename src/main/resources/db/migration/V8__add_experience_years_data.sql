-- Update experience years for existing tutor subjects
UPDATE tutor_subjects ts
INNER JOIN Subjects s ON ts.subject_id = s.subject_id
SET ts.experience_years = 
    CASE s.name
        WHEN 'Mathematics' THEN 5
        WHEN 'Physics' THEN 4
        WHEN 'Programming' THEN 3
        ELSE 2
    END
WHERE ts.tutor_id = (
    SELECT t.tutor_id
    FROM Tutors t
    INNER JOIN Users u ON t.user_id = u.user_id
    WHERE u.email = 'tutor@tcmp.com'
    LIMIT 1
);