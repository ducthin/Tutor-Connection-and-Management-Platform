package com.upsilon.TCMP.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Integer id;
    private Integer senderId;
    private String senderName;
    private String senderRole;
    private Integer receiverId;
    private String receiverName;
    private String receiverRole;
    private String content;
    private LocalDateTime timestamp;
    private Boolean isRead;
    private Integer sessionId;  // Optional, if message is related to a session
}