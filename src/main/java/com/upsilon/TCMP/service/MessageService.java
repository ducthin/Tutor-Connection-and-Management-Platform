package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.MessageDTO;
import com.upsilon.TCMP.dto.MessageCreateDTO;
import com.upsilon.TCMP.dto.MessageThreadDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {
    // Message operations
    MessageDTO sendMessage(MessageCreateDTO createDTO);
    MessageDTO getMessage(Integer messageId);
    void deleteMessage(Integer messageId);
    void markAsRead(Integer messageId);
    void markThreadAsRead(Integer userId, Integer otherUserId);
    
    // Message queries
    List<MessageDTO> getMessageThread(Integer userId, Integer otherUserId);
    List<MessageThreadDTO> getUserThreads(Integer userId);
    List<MessageDTO> getUnreadMessages(Integer userId);
    Long getUnreadMessageCount(Integer userId);
    
    // Message search
    List<MessageDTO> searchMessages(Integer userId, String keyword);
    List<MessageDTO> getMessagesByDateRange(Integer userId, LocalDateTime start, LocalDateTime end);
    
    // Session-related messages
    List<MessageDTO> getSessionMessages(Integer sessionId);
    MessageDTO sendSessionMessage(Integer sessionId, MessageCreateDTO createDTO);
    
    // System messages
    void sendSystemMessage(Integer userId, String content);
    void sendBulkSystemMessage(List<Integer> userIds, String content);
    
    // Message notifications
    void enableNotifications(Integer userId);
    void disableNotifications(Integer userId);
    boolean areNotificationsEnabled(Integer userId);
    
    // Message cleanup
    void archiveOldMessages(LocalDateTime before);
    void deleteThreadPermanently(Integer userId, Integer otherUserId);
}