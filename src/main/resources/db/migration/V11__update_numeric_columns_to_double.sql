ALTER TABLE tutors
    MODIFY hourly_rate DOUBLE(10,2),
    MODIFY rating DOUBLE(3,2);

ALTER TABLE tutor_subjects
    MODIFY hourly_rate DOUBLE(10,2);