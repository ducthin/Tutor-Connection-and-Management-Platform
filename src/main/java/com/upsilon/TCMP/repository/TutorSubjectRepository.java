package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.TutorSubject;
import com.upsilon.TCMP.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TutorSubjectRepository extends JpaRepository<TutorSubject, Integer> {
    
    @Query("SELECT DISTINCT ts FROM TutorSubject ts LEFT JOIN FETCH ts.subject LEFT JOIN FETCH ts.tutor WHERE ts.tutor.id = :tutorId")
    List<TutorSubject> findByTutorId(@Param("tutorId") Integer tutorId);
    
    @Query("SELECT ts FROM TutorSubject ts LEFT JOIN FETCH ts.subject WHERE ts.tutor.id = :tutorId AND ts.subject.id = :subjectId")
    Optional<TutorSubject> findByTutorIdAndSubjectId(@Param("tutorId") Integer tutorId, @Param("subjectId") Integer subjectId);
    
    @Modifying
    @Query("DELETE FROM TutorSubject ts WHERE ts.tutor.id = :tutorId AND ts.subject.id = :subjectId")
    void deleteByTutorIdAndSubjectId(@Param("tutorId") Integer tutorId, @Param("subjectId") Integer subjectId);
    
    List<TutorSubject> findBySubjectId(Integer subjectId);
    
    @Query("SELECT DISTINCT ts FROM TutorSubject ts " +
           "LEFT JOIN FETCH ts.subject s " +
           "WHERE ts.tutor.id = :tutorId AND ts.active = true")
    List<TutorSubject> findActiveByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT COUNT(ts) FROM TutorSubject ts WHERE ts.tutor.id = :tutorId AND ts.active = true")
    Integer countByTutorIdAndActiveTrue(@Param("tutorId") Integer tutorId);
    
    boolean existsByTutorIdAndSubjectId(Integer tutorId, Integer subjectId);
    
    @Query("SELECT AVG(ts.hourlyRate) FROM TutorSubject ts WHERE ts.subject.id = :subjectId")
    Double getAverageHourlyRateForSubject(@Param("subjectId") Integer subjectId);

    @Query("SELECT ts FROM TutorSubject ts " +
           "WHERE (:subjectId IS NULL OR ts.subject.id = :subjectId) " +
           "AND (:maxRate IS NULL OR ts.hourlyRate <= :maxRate) " +
           "AND ts.tutor.isVerified = true " +
           "ORDER BY ts.tutor.rating DESC")
    List<TutorSubject> findAvailableTutorsBySubject(
        @Param("subjectId") Integer subjectId,
        @Param("maxRate") BigDecimal maxRate
    );

    @Query("SELECT DISTINCT ts.subject.id FROM TutorSubject ts " +
           "WHERE ts.tutor.id = :tutorId")
    List<Integer> findSubjectIdsByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT ts FROM TutorSubject ts " +
           "WHERE ts.tutor.id IN " +
           "(SELECT t.id FROM Tutor t JOIN t.availabilities ta " +
           "WHERE ta.dayOfWeek = :dayOfWeek " +
           "AND ta.startTime <= :time " +
           "AND ta.endTime > :time) " +
           "AND (:subjectId IS NULL OR ts.subject.id = :subjectId)")
    List<TutorSubject> findByAvailabilityAndSubject(
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("time") LocalTime time,
        @Param("subjectId") Integer subjectId
    );
}