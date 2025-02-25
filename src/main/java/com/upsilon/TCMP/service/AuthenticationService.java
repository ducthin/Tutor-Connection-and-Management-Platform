package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.UserRegistrationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void register(UserRegistrationDTO registrationDTO) {
        // Registration logic
        validateRegistration(registrationDTO);
        String verificationToken = generateVerificationToken();
        // Save user and send verification email
        sendVerificationEmail(registrationDTO.getEmail(), verificationToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        // Email verification logic
        validateVerificationToken(token);
        // Update user's verified status
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        // Check if email exists
        validateEmail(email);
        String resetToken = generateResetToken();
        // Save reset token
        sendPasswordResetEmail(email, resetToken);
    }

    @Transactional
    public void resetPassword(String token, String password, String confirmPassword) {
        // Validate token and passwords
        validateResetToken(token);
        validatePasswords(password, confirmPassword);
        // Update password
    }

    private void validateRegistration(UserRegistrationDTO dto) {
        // Validation logic
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private void validateEmail(String email) {
        // Check if email exists in the system
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
    }

    private void validatePasswords(String password, String confirmPassword) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private void validateVerificationToken(String token) {
        // Token validation logic
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid verification token");
        }
    }

    private void validateResetToken(String token) {
        // Reset token validation logic
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid reset token");
        }
    }

    private String generateVerificationToken() {
        // Generate verification token logic
        return "verification-token-" + System.currentTimeMillis();
    }

    private String generateResetToken() {
        // Generate reset token logic
        return "reset-token-" + System.currentTimeMillis();
    }

    private void sendVerificationEmail(String email, String token) {
        String subject = "Verify your email address";
        String content = "Please click the link below to verify your email:\n"
                + "http://localhost:8080/verify?token=" + token;
        emailService.sendEmail(email, subject, content);
    }

    private void sendPasswordResetEmail(String email, String token) {
        String subject = "Reset your password";
        String content = "Please click the link below to reset your password:\n"
                + "http://localhost:8080/reset-password?token=" + token;
        emailService.sendEmail(email, subject, content);
    }
}