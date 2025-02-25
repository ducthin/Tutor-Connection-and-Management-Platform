package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Tutor;
import com.upsilon.TCMP.entity.TutorAvailability;
import com.upsilon.TCMP.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TutorAvailabilityRepository extends JpaRepository<TutorAvailability, Integer> {
    List<TutorAvailability> findByTutor(Tutor tutor);
    List<TutorAvailability> findByTutorAndDayOfWeek(Tutor tutor, DayOfWeek dayOfWeek);
    boolean existsByTutorAndDayOfWeekAndStartTimeAndEndTimeAndIsRecurring(
        Tutor tutor, 
        DayOfWeek dayOfWeek, 
        LocalTime startTime, 
        LocalTime endTime, 
        Boolean isRecurring
    );
    void deleteByTutorAndDayOfWeekAndStartTimeAndEndTime(
        Tutor tutor, 
        DayOfWeek dayOfWeek, 
        LocalTime startTime, 
        LocalTime endTime
    );
}