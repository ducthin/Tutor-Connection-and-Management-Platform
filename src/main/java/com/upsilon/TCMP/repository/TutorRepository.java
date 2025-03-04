package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Tutor;
import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Integer> {
    @Query("SELECT t FROM Tutor t LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Tutor> findByIdWithUser(@Param("id") Integer id);
    
    @Override
    @Query("SELECT t FROM Tutor t LEFT JOIN FETCH t.user")
    List<Tutor> findAll();
    
    Optional<Tutor> findByUserId(Integer userId);
    
    Optional<Tutor> findByUser(User user);
    
    @Query("SELECT t FROM Tutor t WHERE t.user.fullName LIKE %:name%")
    List<Tutor> findByUserFullNameContaining(@Param("name") String name);

    @Query("SELECT t FROM Tutor t JOIN t.tutorSubjects ts WHERE ts.subject.id = :subjectId")
    List<Tutor> findByTutorSubjectsSubjectId(@Param("subjectId") Integer subjectId);
    
    @Query("SELECT COUNT(t) FROM Tutor t WHERE t.user.active = true")
    Long countActiveTutors();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutor.id = :tutorId")
    Double getAverageRating(@Param("tutorId") Integer tutorId);

    @Query("SELECT DISTINCT t.id FROM Tutor t " +
           "JOIN t.user u " +
           "JOIN t.tutorSubjects ts " +
           "JOIN ts.subject s " +
           "WHERE ts.active = true " +
           "AND s.active = true " +
           "AND (:keyword IS NULL OR " +
           "   u.fullName LIKE %:keyword% OR " +
           "   t.qualifications LIKE %:keyword% OR " +
           "   t.bio LIKE %:keyword% OR " +
           "   ts.description LIKE %:keyword%) " +
           "AND (:subjectId IS NULL OR s.id = :subjectId) " +
           "AND (:minRating IS NULL OR t.rating >= :minRating) " +
           "AND (:maxRate IS NULL OR t.hourlyRate <= :maxRate) " +
           "AND t.isVerified = true " +
           "AND u.active = true")
    List<Integer> findSearchTutorIds(
        @Param("keyword") String keyword,
        @Param("subjectId") Integer subjectId,
        @Param("minRating") BigDecimal minRating,
        @Param("maxRate") BigDecimal maxRate
    );

    @Query("SELECT DISTINCT t FROM Tutor t " +
           "LEFT JOIN FETCH t.user u " +
           "LEFT JOIN FETCH t.tutorSubjects ts " +
           "LEFT JOIN FETCH ts.subject s " +
           "WHERE t.id IN :tutorIds")
    List<Tutor> findTutorsWithDetails(@Param("tutorIds") List<Integer> tutorIds);

    @Query("SELECT DISTINCT t FROM Tutor t " +
           "JOIN t.tutorSubjects ts " +
           "JOIN TutorAvailability ta ON ta.tutor = t " +
           "WHERE ta.dayOfWeek = :dayOfWeek " +
           "AND ta.startTime <= :time " +
           "AND ta.endTime > :time " +
           "AND (:subjectId IS NULL OR ts.subject.id = :subjectId)")
    List<Tutor> findAvailableTutors(
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("time") LocalTime time,
        @Param("subjectId") Integer subjectId
    );
}
