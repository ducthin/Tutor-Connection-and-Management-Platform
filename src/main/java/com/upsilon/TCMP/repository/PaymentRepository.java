package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Payment;
import com.upsilon.TCMP.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

       @Query("SELECT p FROM Payment p WHERE p.session.id = :sessionId")
       List<Payment> findBySessionId(@Param("sessionId") Integer sessionId);
       
       @Query("SELECT p FROM Payment p WHERE p.session.tutor.id = :tutorId")
       List<Payment> findBySession_Tutor_Id(@Param("tutorId") Integer tutorId);
       
       @Query("SELECT p FROM Payment p WHERE p.session.student.id = :studentId")
       List<Payment> findBySession_Student_Id(@Param("studentId") Integer studentId);
       
       @Query("SELECT p FROM Payment p WHERE p.status = :status")
       List<Payment> findByStatus(@Param("status") PaymentStatus status);
       
       @Query("SELECT p FROM Payment p WHERE p.session.student.id = :studentId OR p.session.tutor.id = :tutorId")
       List<Payment> findBySession_Student_IdOrSession_Tutor_Id(@Param("studentId") Integer studentId, @Param("tutorId") Integer tutorId);
       
       @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
       List<Payment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
       
       @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = :status")
       Double sumAmountByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("status") PaymentStatus status);

       @Query("SELECT p.paymentMethod as method, SUM(p.amount) as total FROM Payment p WHERE p.status = 'COMPLETED' GROUP BY p.paymentMethod")
       Map<String, Double> sumAmountByPaymentMethod();

       @Query("SELECT p.session.tutor.id as tutorId, SUM(p.tutorShare) as total FROM Payment p WHERE p.status = 'COMPLETED' GROUP BY p.session.tutor.id")
       Map<Integer, Double> sumTutorShareGroupByTutor();

       @Query("SELECT p.status as status, COUNT(p) as count FROM Payment p GROUP BY p.status")
       Map<PaymentStatus, Long> countByStatusGroupByStatus();
       
       @Query("SELECT SUM(p.tutorShare) FROM Payment p WHERE p.session.tutor.id = :tutorId AND p.createdAt BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
       Double sumTutorShareByTutorAndDateRange(@Param("tutorId") Integer tutorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
       @Query("SELECT p FROM Payment p WHERE p.session.student.id = :studentId")
       List<Payment> findByStudentId(@Param("studentId") Integer studentId);
       
       @Query("SELECT SUM(p.tutorShare) FROM Payment p WHERE p.session.student.id = :studentId AND p.status = 'COMPLETED'")
       Double calculateTotalSpentByStudent(@Param("studentId") Integer studentId);
       
       @Query("SELECT SUM(p.tutorShare) FROM Payment p WHERE p.session.tutor.id = :tutorId AND p.status = 'COMPLETED'")
       Double calculateTotalEarningsForTutor(@Param("tutorId") Integer tutorId);
       
       @Query("SELECT SUM(p.tutorShare) FROM Payment p WHERE p.session.tutor.id = :tutorId " +
              "AND p.status = 'COMPLETED' " +
              "AND p.paymentDate BETWEEN :startDate AND :endDate")
       Double calculateMonthlyEarningsForTutor(@Param("tutorId") Integer tutorId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}