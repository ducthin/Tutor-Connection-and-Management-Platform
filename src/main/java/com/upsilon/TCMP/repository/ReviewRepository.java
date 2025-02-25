package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    @Query("SELECT r FROM Review r WHERE r.tutor.id = :tutorId")
    List<Review> findByTutorId(@Param("tutorId") Integer tutorId);
    
    @Query("SELECT r FROM Review r WHERE r.student.id = :studentId")
    List<Review> getStudentReviews(@Param("studentId") Integer studentId);
    
    @Query("SELECT r FROM Review r WHERE r.session.id = :sessionId")
    List<Review> findBySessionId(@Param("sessionId") Integer sessionId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutor.id = :tutorId")
    Double getAverageRatingByTutorId(@Param("tutorId") Integer tutorId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.tutor.id = :tutorId")
    Long countByTutorId(@Param("tutorId") Integer tutorId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.tutor.id = :tutorId AND r.rating >= :minRating")
    Long countByTutorIdAndMinRating(@Param("tutorId") Integer tutorId, @Param("minRating") Integer minRating);
    
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.session.id = :sessionId")
    boolean existsBySessionId(@Param("sessionId") Integer sessionId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutor.id = :tutorId")
    Double calculateAverageRating(@Param("tutorId") Integer tutorId);

    @Query("SELECT r.tutor.id as tutorId, AVG(r.rating) as avgRating " +
           "FROM Review r " +
           "WHERE r.student.id = :studentId AND r.rating IS NOT NULL " +
           "GROUP BY r.tutor.id")
    List<Object[]> getStudentTutorRatingsDistribution(@Param("studentId") Integer studentId);

    @Query(value = "SELECT " +
            "r.id, r.session_id, r.student_id, r.tutor_id, " +
            "r.rating, r.comment, r.created_at, r.updated_at, r.is_public " +
            "FROM reviews r " +
            "WHERE r.tutor_id = :tutorId " +
            "ORDER BY r.created_at DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Review> findRecentReviewsByTutorId(@Param("tutorId") Integer tutorId, @Param("limit") int limit);

    List<Review> findByStudentId(Integer studentId);
}
