-- Insert Monday availability for tutors 1-3
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
VALUES 
(1, 'MONDAY', '09:00:00', '17:00:00', true),
(2, 'MONDAY', '09:00:00', '17:00:00', true),
(3, 'MONDAY', '09:00:00', '17:00:00', true);

-- Insert Wednesday availability for tutors 1-3
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
VALUES 
(1, 'WEDNESDAY', '13:00:00', '21:00:00', true),
(2, 'WEDNESDAY', '13:00:00', '21:00:00', true),
(3, 'WEDNESDAY', '13:00:00', '21:00:00', true);

-- Insert Friday availability for tutors 1-3
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
VALUES 
(1, 'FRIDAY', '10:00:00', '18:00:00', true),
(2, 'FRIDAY', '10:00:00', '18:00:00', true),
(3, 'FRIDAY', '10:00:00', '18:00:00', true);

-- Add varied availability for tutor 4
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
VALUES (4, 'TUESDAY', '14:00:00', '22:00:00', true);

-- Add varied availability for tutor 5
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
VALUES (5, 'THURSDAY', '08:00:00', '16:00:00', true);

-- Create indexes for efficient querying
CREATE INDEX idx_availability_day_time ON tutor_availability(day_of_week, start_time, end_time);
CREATE INDEX idx_availability_tutor_day ON tutor_availability(tutor_id, day_of_week);