package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Session;
import com.upsilon.TCMP.entity.Student;
import com.upsilon.TCMP.entity.Tutor;
import com.upsilon.TCMP.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    
    @Query("SELECT s FROM Session s WHERE s.student.id = :studentId AND s.startTime > CURRENT_TIMESTAMP ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessionsByStudentId(@Param("studentId") Integer studentId);
    
    @Query("SELECT s FROM Session s WHERE s.tutor.id = :tutorId AND s.startTime > CURRENT_TIMESTAMP ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessionsByTutorId(@Param("tutorId") Integer tutorId);

    List<Session> findAllByStudentId(Integer studentId);
    
    List<Session> findAllByTutorId(Integer tutorId);
    
    List<Session> findByTutor(Tutor tutor);
    
    Long countByTutor_Id(Integer tutorId);
    
    @Query("SELECT COUNT(s) FROM Session s WHERE s.tutor.id = :tutorId AND s.status = :status")
    Long countByTutorIdAndStatus(@Param("tutorId") Integer tutorId, @Param("status") SessionStatus status);
    
    @Query("SELECT s FROM Session s WHERE s.startTime BETWEEN :start AND :end")
    List<Session> findSessionsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Session s ORDER BY s.id DESC")
    List<Session> findRecentSessions(@Param("limit") int limit);
    
    @Query("SELECT COUNT(s) FROM Session s WHERE s.status = :status")
    Long countByStatus(@Param("status") SessionStatus status);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.tutor.id = :tutorId AND s.status = 'COMPLETED'")
    Integer countCompletedSessionsByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.tutor.id = :tutorId AND s.status = 'SCHEDULED' AND s.startTime > CURRENT_TIMESTAMP")
    Integer countUpcomingSessionsByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.tutor.id = :tutorId AND s.status = 'CANCELLED'")
    Integer countCancelledSessionsByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT COUNT(DISTINCT s.student.id) FROM Session s WHERE s.tutor.id = :tutorId AND s.status = 'COMPLETED'")
    Integer countUniqueStudentsByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT DISTINCT s.student FROM Session s WHERE s.tutor.id = :tutorId AND s.status = 'COMPLETED' GROUP BY s.student HAVING COUNT(s) >= 3")
    List<Student> findRegularStudentsByTutorId(@Param("tutorId") Integer tutorId);
    
    List<Session> findByStudent_Id(Integer studentId);
    
    @Query("SELECT s FROM Session s WHERE s.student.id = :studentId AND s.startTime > :now AND s.status = 'SCHEDULED'")
    List<Session> findUpcomingSessionsByStudent(@Param("studentId") Integer studentId, @Param("now") LocalDateTime now);
    
    List<Session> findByStudent_IdAndStatus(Integer studentId, SessionStatus status);
    
    @Query("SELECT s.subject.name as subject, COUNT(s) * 2 as hours " +  // Assuming each session is 2 hours
           "FROM Session s " +
           "WHERE s.student.id = :studentId " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY s.subject.name")
    List<Object[]> getStudentSubjectHoursDistribution(@Param("studentId") Integer studentId);

    @Query("SELECT s FROM Session s WHERE s.tutor.id = :tutorId AND s.status = com.upsilon.TCMP.enums.SessionStatus.PENDING ORDER BY s.startTime ASC")
    List<Session> findPendingSessionsByTutorId(@Param("tutorId") Integer tutorId);
}