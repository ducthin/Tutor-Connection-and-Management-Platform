package com.upsilon.TCMP.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageThreadDTO {
    private Integer otherUserId;
    private String otherUserName;
    private String otherUserRole;
    private LocalDateTime lastMessageTime;
    private String lastMessagePreview;
    private Integer unreadCount;
    private List<MessageDTO> messages;
    
    // Additional fields for context
    private Integer activeSessionId;
    private String sessionStatus;
    private LocalDateTime nextSessionTime;
    private Double sessionPrice;
}