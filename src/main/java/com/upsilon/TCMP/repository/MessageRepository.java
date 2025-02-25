package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    
    // Basic queries
    List<Message> findByReceiverIdAndIsReadFalse(Integer receiverId);
    Long countByReceiverIdAndIsReadFalse(Integer receiverId);
    List<Message> findBySentAtBefore(LocalDateTime dateTime);
    
    // Custom queries for conversation between two users
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
           "ORDER BY m.sentAt")
    List<Message> findByUserPair(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.receiver.id = :otherId AND m.isRead = false)")
    List<Message> findByReceiverIdAndSenderIdAndIsReadFalse(@Param("userId") Integer userId, @Param("otherId") Integer otherId);

    // Query to fetch all messages for a user
    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Message> findByUserInvolved(@Param("userId") Integer userId);
    
    // Search messages by content
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Message> findByUserInvolvedAndContentContaining(@Param("userId") Integer userId, @Param("keyword") String keyword);
    
    // Get messages within date range
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "m.sentAt BETWEEN :startDate AND :endDate")
    List<Message> findByUserInvolvedAndSentAtBetween(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Get messages for a specific session
    @Query("SELECT m FROM Message m WHERE m.session.id = :sessionId ORDER BY m.sentAt")
    List<Message> findBySessionId(@Param("sessionId") Integer sessionId);
    
    // Delete all messages between two users
    @Query("DELETE FROM Message m WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)")
    void deleteByUserPair(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
}