package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.UserDTO;
import com.upsilon.TCMP.dto.UserLoginDTO;
import com.upsilon.TCMP.dto.UserRegistrationDTO;
import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.entity.Student;
import com.upsilon.TCMP.entity.Tutor;
import com.upsilon.TCMP.entity.VerificationToken;
import com.upsilon.TCMP.enums.Role;
import com.upsilon.TCMP.service.EmailService;
import com.upsilon.TCMP.service.IAuthenticationService;
import com.upsilon.TCMP.config.JwtTokenProvider;
import com.upsilon.TCMP.repository.UserRepository;
import com.upsilon.TCMP.repository.StudentRepository;
import com.upsilon.TCMP.repository.TutorRepository;
import com.upsilon.TCMP.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    public UserDTO login(UserLoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()
                )
            );

            User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setFullName(user.getFullName());
            userDTO.setRole(user.getRole());
            userDTO.setToken(tokenProvider.generateToken(user.getEmail()));

            return userDTO;
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public void register(UserRegistrationDTO registrationDTO) {
        logger.debug("Starting registration process for email: {}", registrationDTO.getEmail());
        
        // Validate registration data
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFullName(registrationDTO.getFullName());
        user.setPhoneNumber(registrationDTO.getPhone());
        user.setRole(registrationDTO.getRoleEnum());
        user.setActive(true);
        user.setEmailVerified(false);
        
        // Save the user first to get the ID
        user = userRepository.save(user);
        logger.debug("User created with ID: {}", user.getId());

        // Create role-specific entity
        if (user.getRole() == Role.ROLE_STUDENT) {
            Student student = new Student();
            student.setUser(user);
            studentRepository.save(student);
            logger.debug("Student profile created for user ID: {}", user.getId());
        } else if (user.getRole() == Role.ROLE_TUTOR) {
            Tutor tutor = new Tutor();
            tutor.setUser(user);
            tutor.setIsVerified(false);
            tutorRepository.save(tutor);
            logger.debug("Tutor profile created for user ID: {}", user.getId());
        }

        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        
        tokenRepository.save(verificationToken);
        logger.debug("Verification token created for user ID: {}", user.getId());

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), token);
        logger.debug("Verification email sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new IllegalStateException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        logger.debug("Email verified for user ID: {}", user.getId());

        // Delete the used token
        tokenRepository.delete(verificationToken);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    @Override
    @Transactional
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Delete any existing verification tokens
        tokenRepository.deleteByUserIdAndType(user.getId(), VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Create new verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        
        tokenRepository.save(verificationToken);

        // Send new verification email
        emailService.sendVerificationEmail(user.getEmail(), token);
        logger.debug("New verification email sent to: {}", user.getEmail());
    }

    // Other methods remain the same...
    @Override
    public String generateJwtToken(UserDTO userDTO) {
        return tokenProvider.generateToken(userDTO.getEmail());
    }

    @Override
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    @Override
    public void revokeToken(String token) {
        // Implementation for token revocation if needed
    }

    @Override
    public String refreshToken(String token) {
        return tokenProvider.refreshToken(token);
    }

    @Override
    public void logout(Integer userId) {
        // Implementation for logout if needed
    }

    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete any existing reset tokens
        tokenRepository.deleteByUserIdAndType(user.getId(), VerificationToken.TokenType.PASSWORD_RESET);

        // Create new reset token
        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setType(VerificationToken.TokenType.PASSWORD_RESET);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        
        tokenRepository.save(resetToken);
        logger.debug("Password reset token created for user ID: {}", user.getId());

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        VerificationToken resetToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalStateException("Reset token has expired");
        }

        if (resetToken.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            throw new IllegalArgumentException("Invalid token type");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        logger.debug("Password reset completed for user ID: {}", user.getId());

        // Delete the used token
        tokenRepository.delete(resetToken);
    }
}