package com.upsilon.TCMP.dto;

import lombok.Data;

@Data
public class MessageCreateDTO {
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private Integer sessionId;  // Optional, if message is related to a session
}