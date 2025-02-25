package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.PaymentDTO;
import com.upsilon.TCMP.entity.Payment;
import com.upsilon.TCMP.entity.Session;
import com.upsilon.TCMP.enums.PaymentStatus;
import com.upsilon.TCMP.repository.PaymentRepository;
import com.upsilon.TCMP.repository.SessionRepository;
import com.upsilon.TCMP.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final double PLATFORM_FEE_PERCENTAGE = 0.10; // 10%
    private static final double TUTOR_SHARE_PERCENTAGE = 0.80; // 80%

    private final PaymentRepository paymentRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, SessionRepository sessionRepository) {
        this.paymentRepository = paymentRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public PaymentDTO createPayment(Integer sessionId, Double amount) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Payment payment = new Payment();
        payment.setSession(session);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPlatformFee(BigDecimal.valueOf(calculatePlatformFee(amount)));
        payment.setTutorShare(BigDecimal.valueOf(getTutorShare(amount)));

        return convertToDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO updatePaymentStatus(Integer id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(status);
        return convertToDTO(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentDTO> getPaymentsBySessionId(Integer sessionId) {
        return paymentRepository.findBySessionId(sessionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByTutorId(Integer tutorId) {
        return paymentRepository.findBySession_Tutor_Id(tutorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByStudentId(Integer studentId) {
        return paymentRepository.findBySession_Student_Id(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByUser(Integer userId) {
        return paymentRepository.findBySession_Student_IdOrSession_Tutor_Id(userId, userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsBySession(Integer sessionId) {
        return getPaymentsBySessionId(sessionId);
    }

    @Override
    public List<PaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByCreatedAtBetween(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO processPayment(Integer paymentId, String paymentMethod) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.COMPLETED);
        return convertToDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentDTO refundPayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(payment.getAmount());
        return convertToDTO(paymentRepository.save(payment));
    }

    @Override
    public boolean isPaymentValid(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return payment.getStatus() == PaymentStatus.COMPLETED;
    }

    @Override
    public boolean canRefundPayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return payment.getStatus() == PaymentStatus.COMPLETED 
            && payment.getRefundAmount() == null;
    }

    @Override
    public Double calculatePlatformFee(Double amount) {
        return amount * PLATFORM_FEE_PERCENTAGE;
    }

    @Override
    public Double getTutorShare(Double amount) {
        return amount * TUTOR_SHARE_PERCENTAGE;
    }

    @Override
    public Double calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.sumAmountByDateRangeAndStatus(
                startDate, endDate, PaymentStatus.COMPLETED);
    }

    @Override
    public Double calculateTutorEarnings(Integer tutorId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.sumTutorShareByTutorAndDateRange(tutorId, startDate, endDate);
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethod() {
        return paymentRepository.sumAmountByPaymentMethod();
    }

    @Override
    public Map<PaymentStatus, Long> getPaymentStatusDistribution() {
        return paymentRepository.countByStatusGroupByStatus();
    }

    @Override
    public Map<Integer, Double> getTutorEarningsDistribution() {
        return paymentRepository.sumTutorShareGroupByTutor();
    }

    @Override
    public Map<String, Object> getPaymentSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("platformFeePercentage", PLATFORM_FEE_PERCENTAGE);
        settings.put("tutorSharePercentage", TUTOR_SHARE_PERCENTAGE);
        return settings;
    }

    @Override
    public void updatePaymentSettings(Map<String, Object> settings) {
        // Implementation for updating payment settings would go here
        // This might involve updating configuration or database settings
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setSessionId(payment.getSession().getId());
        dto.setAmount(payment.getAmount().doubleValue());
        dto.setStatus(payment.getStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPlatformFee(payment.getPlatformFee().doubleValue());
        dto.setTutorShare(payment.getTutorShare().doubleValue());
        if (payment.getRefundAmount() != null) {
            dto.setRefundAmount(payment.getRefundAmount().doubleValue());
        }
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}