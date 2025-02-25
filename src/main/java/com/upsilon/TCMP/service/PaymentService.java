package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.PaymentDTO;
import com.upsilon.TCMP.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    PaymentDTO createPayment(Integer sessionId, Double amount);
    PaymentDTO getPaymentById(Integer id);
    PaymentDTO updatePaymentStatus(Integer id, PaymentStatus status);
    List<PaymentDTO> getPaymentsBySessionId(Integer sessionId);
    List<PaymentDTO> getPaymentsByTutorId(Integer tutorId);
    List<PaymentDTO> getPaymentsByStudentId(Integer studentId);
    List<PaymentDTO> getPaymentsByStatus(PaymentStatus status);
    List<PaymentDTO> getPaymentsByUser(Integer userId);
    List<PaymentDTO> getPaymentsBySession(Integer sessionId);
    List<PaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    PaymentDTO processPayment(Integer paymentId, String paymentMethod);
    PaymentDTO refundPayment(Integer paymentId);
    
    boolean isPaymentValid(Integer paymentId);
    boolean canRefundPayment(Integer paymentId);
    
    Double calculatePlatformFee(Double amount);
    Double getTutorShare(Double amount);
    Double calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
    Double calculateTutorEarnings(Integer tutorId, LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Double> getRevenueByPaymentMethod();
    Map<PaymentStatus, Long> getPaymentStatusDistribution();
    Map<Integer, Double> getTutorEarningsDistribution();
    
    Map<String, Object> getPaymentSettings();
    void updatePaymentSettings(Map<String, Object> settings);
}