-- Create favorite tutors table
CREATE TABLE IF NOT EXISTS student_favorite_tutors (
    student_id INT NOT NULL,
    tutor_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, tutor_id),
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (tutor_id) REFERENCES Tutors(tutor_id)
);

-- Add indexes for better performance
CREATE INDEX idx_student_favorites ON student_favorite_tutors(student_id);
CREATE INDEX idx_tutor_favorites ON student_favorite_tutors(tutor_id);

-- Add some sample favorite tutors for the demo student
INSERT INTO student_favorite_tutors (student_id, tutor_id)
SELECT s.student_id, top_tutors.tutor_id
FROM Students s
CROSS JOIN (
    SELECT t.tutor_id
    FROM Tutors t
    JOIN Users u ON t.user_id = u.user_id
    WHERE u.email LIKE '%.%@example.com'
    ORDER BY t.rating DESC
    LIMIT 2
) top_tutors
WHERE s.user_id = (SELECT user_id FROM Users WHERE email = 'student@tcmp.com');

-- Add additional indexes for tutor search performance
CREATE INDEX idx_tutors_rating_verified ON Tutors(rating DESC, is_verified);
CREATE INDEX idx_users_fullname ON Users(full_name);