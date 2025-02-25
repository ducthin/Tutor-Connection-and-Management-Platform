package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PaymentDTO {
    private Integer id;
    private Integer sessionId;
    private Double amount;
    private PaymentStatus status;
    private String paymentMethod;
    private Double platformFee;
    private Double tutorShare;
    private Double refundAmount;
    private String refundReason;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String transactionId;
}