package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Student;
import com.upsilon.TCMP.entity.Tutor;
import com.upsilon.TCMP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    
    Optional<Student> findByUserId(Integer userId);
    
    @Query("SELECT s FROM Student s JOIN FETCH s.user WHERE s.id = :id")
    Optional<Student> findByIdWithUser(@Param("id") Integer id);

    @Query("SELECT s FROM Student s JOIN FETCH s.user WHERE s.user.email = :email")
    Optional<Student> findByUserEmailWithUser(@Param("email") String email);
    
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.sessions sess " +
           "LEFT JOIN FETCH sess.tutor t " +
           "LEFT JOIN FETCH t.user " +
           "LEFT JOIN FETCH sess.subject " +
           "WHERE s.user.id = :userId")
    Optional<Student> findByUserIdWithSessions(@Param("userId") Integer userId);
    
    @Query("SELECT COUNT(DISTINCT s.subject.id) FROM Session s WHERE s.student.id = :studentId")
    int countActiveSubjects(@Param("studentId") Integer studentId);
    
    @Query("SELECT t FROM Student s JOIN s.favoriteTutors t JOIN FETCH t.user WHERE s.id = :studentId")
    List<Tutor> findFavoriteTutors(@Param("studentId") Integer studentId);
    
    // Find student with favorite tutors eagerly loaded
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.favoriteTutors WHERE s.id = :studentId")
    Optional<Student> findByIdWithFavoriteTutors(@Param("studentId") Integer studentId);
    
    // Check if tutor is in favorites
    @Query("SELECT COUNT(s) > 0 FROM Student s JOIN s.favoriteTutors t WHERE s.id = :studentId AND t.id = :tutorId")
    boolean isTutorInFavorites(@Param("studentId") Integer studentId, @Param("tutorId") Integer tutorId);
    
    @Query("SELECT DISTINCT ses.subject.id FROM Session ses WHERE ses.student.id = :studentId")
    List<Integer> findEnrolledSubjectIds(@Param("studentId") Integer studentId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.student.id = :studentId")
    Double findAverageRating(@Param("studentId") Integer studentId);
    
    @Query("SELECT COUNT(s) FROM Session s WHERE s.student.id = :studentId AND s.status = 'COMPLETED'")
    int countCompletedSessions(@Param("studentId") Integer studentId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p JOIN p.session s WHERE s.student.id = :studentId AND p.status = 'COMPLETED'")
    Double sumTotalPayments(@Param("studentId") Integer studentId);
    
    @Query("SELECT s.hasSubscription FROM Student s WHERE s.id = :studentId")
    boolean hasActiveSubscription(@Param("studentId") Integer studentId);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Payment p JOIN p.session s WHERE s.student.id = :studentId AND p.status = 'PENDING'")
    boolean hasOutstandingPayments(@Param("studentId") Integer studentId);
}