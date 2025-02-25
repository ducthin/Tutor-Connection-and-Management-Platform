-- TutorAvailability table
CREATE TABLE IF NOT EXISTS tutor_availability (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tutor_id INT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_recurring BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (tutor_id) REFERENCES Tutors(tutor_id)
);

-- Create index for tutor availability
CREATE INDEX idx_tutor_availability_tutor ON tutor_availability(tutor_id);

-- Insert sample subjects
INSERT INTO Subjects (name, category, description) VALUES 
('Mathematics', 'Science', 'Mathematics including algebra, calculus, and geometry'),
('Physics', 'Science', 'Physics fundamentals and advanced concepts'),
('Chemistry', 'Science', 'Chemistry including organic and inorganic'),
('English', 'Languages', 'English language and literature'),
('Programming', 'Technology', 'Computer programming and software development');