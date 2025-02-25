package com.upsilon.TCMP.service;

import com.upsilon.TCMP.entity.Message;
import java.util.Map;

public interface NotificationService {
    void notifyNewMessage(Message message);
    
    void sendVerificationNotification(Integer userId);
    
    void sendEmailWithTemplate(String email, String templateName, Map<String, String> params);
    
    void sendEmail(String to, String subject, String content);
}