-- Drop foreign keys first to avoid constraints during rename
ALTER TABLE tutor_availability DROP FOREIGN KEY IF EXISTS tutor_availability_ibfk_1;
ALTER TABLE tutor_subjects DROP FOREIGN KEY IF EXISTS tutor_subjects_ibfk_1;
ALTER TABLE sessions DROP FOREIGN KEY IF EXISTS sessions_ibfk_2;
ALTER TABLE reviews DROP FOREIGN KEY IF EXISTS reviews_ibfk_3;

-- Rename tables to lowercase if they exist in uppercase
RENAME TABLE IF EXISTS Tutors TO tutors_temp;
RENAME TABLE IF EXISTS tutors_temp TO tutors;

RENAME TABLE IF EXISTS Users TO users_temp;
RENAME TABLE IF EXISTS users_temp TO users;

RENAME TABLE IF EXISTS Students TO students_temp;
RENAME TABLE IF EXISTS students_temp TO students;

RENAME TABLE IF EXISTS Subjects TO subjects_temp;
RENAME TABLE IF EXISTS subjects_temp TO subjects;

RENAME TABLE IF EXISTS Sessions TO sessions_temp;
RENAME TABLE IF EXISTS sessions_temp TO sessions;

RENAME TABLE IF EXISTS Messages TO messages_temp;
RENAME TABLE IF EXISTS messages_temp TO messages;

RENAME TABLE IF EXISTS Reviews TO reviews_temp;
RENAME TABLE IF EXISTS reviews_temp TO reviews;

RENAME TABLE IF EXISTS Payments TO payments_temp;
RENAME TABLE IF EXISTS payments_temp TO payments;

-- Recreate foreign keys with correct case
ALTER TABLE tutor_availability ADD CONSTRAINT tutor_availability_ibfk_1
    FOREIGN KEY (tutor_id) REFERENCES tutors(tutor_id);
    
ALTER TABLE tutor_subjects ADD CONSTRAINT tutor_subjects_ibfk_1
    FOREIGN KEY (tutor_id) REFERENCES tutors(tutor_id);
    
ALTER TABLE sessions ADD CONSTRAINT sessions_ibfk_2
    FOREIGN KEY (tutor_id) REFERENCES tutors(tutor_id);
    
ALTER TABLE reviews ADD CONSTRAINT reviews_ibfk_3
    FOREIGN KEY (tutor_id) REFERENCES tutors(tutor_id);