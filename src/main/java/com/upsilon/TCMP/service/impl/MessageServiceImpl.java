package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.MessageDTO;
import com.upsilon.TCMP.dto.MessageCreateDTO;
import com.upsilon.TCMP.dto.MessageThreadDTO;
import com.upsilon.TCMP.entity.Message;
import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.entity.Session;
import com.upsilon.TCMP.repository.MessageRepository;
import com.upsilon.TCMP.repository.UserRepository;
import com.upsilon.TCMP.repository.SessionRepository;
import com.upsilon.TCMP.service.MessageService;
import com.upsilon.TCMP.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final NotificationService notificationService;

    @Override
    public MessageDTO sendMessage(MessageCreateDTO createDTO) {
        User sender = userRepository.findById(createDTO.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        User receiver = userRepository.findById(createDTO.getReceiverId())
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));
        
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(createDTO.getContent());
        
        if (createDTO.getSessionId() != null) {
            Session session = sessionRepository.findById(createDTO.getSessionId())
                    .orElseThrow(() -> new EntityNotFoundException("Session not found"));
            message.setSession(session);
        }
        
        Message savedMessage = messageRepository.save(message);
        notificationService.notifyNewMessage(savedMessage);
        
        return convertToDTO(savedMessage);
    }

    @Override
    public MessageDTO getMessage(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        return convertToDTO(message);
    }

    @Override
    public void deleteMessage(Integer messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public void markAsRead(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    @Override
    public void markThreadAsRead(Integer userId, Integer otherUserId) {
        List<Message> unreadMessages = messageRepository
                .findByReceiverIdAndSenderIdAndIsReadFalse(userId, otherUserId);
        unreadMessages.forEach(message -> message.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    @Override
    public List<MessageDTO> getMessageThread(Integer userId, Integer otherUserId) {
        return messageRepository.findByUserPair(userId, otherUserId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageThreadDTO> getUserThreads(Integer userId) {
        List<Message> allMessages = messageRepository.findByUserInvolved(userId);
        
        Map<Integer, List<Message>> messagesByUser = new HashMap<>();
        for (Message message : allMessages) {
            Integer otherUserId = message.getSender().getId().equals(userId) ? 
                    message.getReceiver().getId() : message.getSender().getId();
            
            messagesByUser.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(message);
        }
        
        return messagesByUser.entrySet().stream()
                .map(entry -> createMessageThreadDTO(userId, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getUnreadMessages(Integer userId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadMessageCount(Integer userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    @Override
    public List<MessageDTO> searchMessages(Integer userId, String keyword) {
        return messageRepository.findByUserInvolvedAndContentContaining(userId, keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getMessagesByDateRange(Integer userId, LocalDateTime start, LocalDateTime end) {
        return messageRepository.findByUserInvolvedAndSentAtBetween(userId, start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getSessionMessages(Integer sessionId) {
        return messageRepository.findBySessionId(sessionId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MessageDTO sendSessionMessage(Integer sessionId, MessageCreateDTO createDTO) {
        createDTO.setSessionId(sessionId);
        return sendMessage(createDTO);
    }

    @Override
    public void sendSystemMessage(Integer userId, String content) {
        // System messages don't have a sender
        MessageCreateDTO createDTO = new MessageCreateDTO();
        createDTO.setReceiverId(userId);
        createDTO.setContent(content);
        sendMessage(createDTO);
    }

    @Override
    public void sendBulkSystemMessage(List<Integer> userIds, String content) {
        userIds.forEach(userId -> sendSystemMessage(userId, content));
    }

    @Override
    public void enableNotifications(Integer userId) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Implement notification preference logic here
    }

    @Override
    public void disableNotifications(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Implement notification preference logic here
    }

    @Override
    public boolean areNotificationsEnabled(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return true; // Implement actual logic based on user preferences
    }

    @Override
    public void archiveOldMessages(LocalDateTime before) {
        // TODO: Implement actual archiving logic
        messageRepository.findBySentAtBefore(before)
            .forEach(message -> message.setArchived(true));
    }

    @Override
    public void deleteThreadPermanently(Integer userId, Integer otherUserId) {
        messageRepository.deleteByUserPair(userId, otherUserId);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setSenderRole(message.getSender().getRole().toString());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverName(message.getReceiver().getFullName());
        dto.setReceiverRole(message.getReceiver().getRole().toString());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getSentAt());
        dto.setIsRead(message.getIsRead());
        if (message.getSession() != null) {
            dto.setSessionId(message.getSession().getId());
        }
        return dto;
    }

    private MessageThreadDTO createMessageThreadDTO(Integer userId, Integer otherUserId, List<Message> messages) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Message lastMessage = messages.stream()
                .max(Comparator.comparing(Message::getSentAt))
                .orElse(null);
        
        long unreadCount = messages.stream()
                .filter(m -> m.getReceiver().getId().equals(userId) && !m.getIsRead())
                .count();
        
        MessageThreadDTO dto = new MessageThreadDTO();
        dto.setOtherUserId(otherUserId);
        dto.setOtherUserName(otherUser.getFullName());
        dto.setOtherUserRole(otherUser.getRole().toString());
        
        if (lastMessage != null) {
            dto.setLastMessageTime(lastMessage.getSentAt());
            dto.setLastMessagePreview(lastMessage.getContent().substring(0, Math.min(50, lastMessage.getContent().length())));
        }
        
        dto.setUnreadCount((int) unreadCount);
        dto.setMessages(messages.stream().map(this::convertToDTO).collect(Collectors.toList()));
        
        // Additional session-related information could be added here if needed
        return dto;
    }
}