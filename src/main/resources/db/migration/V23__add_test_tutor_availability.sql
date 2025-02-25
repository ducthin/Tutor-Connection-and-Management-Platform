-- Add Monday availability for tutors 1-3
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
SELECT tutor_id, 'MONDAY', '09:00:00', '17:00:00', true
FROM Tutors
WHERE tutor_id <= 3;

-- Add Wednesday availability for tutors 1-3
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
SELECT tutor_id, 'WEDNESDAY', '13:00:00', '21:00:00', true
FROM Tutors
WHERE tutor_id <= 3;

-- Add Friday availability for tutors 1-3
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
SELECT tutor_id, 'FRIDAY', '10:00:00', '18:00:00', true
FROM Tutors
WHERE tutor_id <= 3;

-- Add Tuesday availability for tutor 4
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
SELECT tutor_id, 'TUESDAY', '14:00:00', '22:00:00', true
FROM Tutors
WHERE tutor_id = 4;

-- Add Thursday availability for tutor 5
INSERT INTO tutor_availability (tutor_id, day_of_week, start_time, end_time, is_recurring)
SELECT tutor_id, 'THURSDAY', '08:00:00', '16:00:00', true
FROM Tutors
WHERE tutor_id = 5;