package com.upsilon.TCMP.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendWelcomeEmail(String to, String name);
    void sendPasswordResetEmail(String to, String token);
    void sendEmail(String to, String subject, String content);
}