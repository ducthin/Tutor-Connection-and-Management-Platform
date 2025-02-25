package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {
    // User management
    void verifyTutor(Integer tutorId);
    void deactivateUser(Integer userId);
    void reactivateUser(Integer userId);
    void updateUserRole(Integer userId, String role);
    List<UserDTO> getUnverifiedTutors();
    
    // Content moderation
    void reviewFlaggedContent(Integer contentId, boolean approved, String moderationNote);
    List<ReviewDTO> getFlaggedReviews();
    List<MessageDTO> getFlaggedMessages();
    
    // System monitoring
    Map<String, Long> getUserStatistics();
    Map<String, Double> getFinancialStatistics();
    Map<String, Long> getSessionStatistics();
    List<Object> getSystemLogs(LocalDateTime start, LocalDateTime end);
    List<Object> getErrorLogs(LocalDateTime start, LocalDateTime end);
    
    // Platform settings
    void updatePlatformSettings(Map<String, Object> settings);
    Map<String, Object> getPlatformSettings();
    void updatePaymentSettings(Map<String, Object> settings);
    void updateEmailTemplates(Map<String, String> templates);
    
    // Data management
    void backupData(String location);
    void restoreData(String backupFile);
    void cleanupOldData(LocalDateTime before);
    void exportData(String format, String[] tables, String destination);
    
    // System maintenance
    void scheduleSystemMaintenance(LocalDateTime startTime, LocalDateTime endTime);
    void cancelSystemMaintenance(String maintenanceId);
    List<Object> getScheduledMaintenance();
    
    // Notifications
    void sendSystemNotification(String message, List<String> roles);
    void sendEmailBroadcast(String subject, String content, List<String> roles);
    
    // Reports
    Map<String, Object> generateUserActivityReport(LocalDateTime start, LocalDateTime end);
    Map<String, Object> generateFinancialReport(LocalDateTime start, LocalDateTime end);
    Map<String, Object> generateTutorPerformanceReport(LocalDateTime start, LocalDateTime end);
    Map<String, Object> generateSystemHealthReport();
}