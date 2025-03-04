-- Drop and recreate foreign key for user-tutor relationship
ALTER TABLE tutors DROP FOREIGN KEY IF EXISTS tutors_ibfk_1;

ALTER TABLE tutors ADD CONSTRAINT tutors_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(user_id);