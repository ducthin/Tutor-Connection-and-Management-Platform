package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.enums.Role;
import com.upsilon.TCMP.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/test-auth")
    public String testAuth() {
        // Create a test user with known credentials
        User user = new User();
        user.setEmail("test@tcmp.com");
        String rawPassword = "test123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        user.setFullName("Test User");
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);
        user.setEmailVerified(true);
        
        // Save the user
        user = userRepository.save(user);
        
        // Test if we can verify the password
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        
        // Get existing admin user and test password
        User adminUser = userRepository.findByEmail("admin@tcmp.com")
            .orElseThrow(() -> new RuntimeException("Admin user not found"));
        boolean adminMatches = passwordEncoder.matches("admin123", adminUser.getPassword());
        
        return String.format("""
            Test User:
            Raw password: %s
            Encoded password: %s
            Password matches: %s
            
            Admin User:
            Expected password: admin123
            Stored hash: %s
            Password matches: %s
            """, 
            rawPassword, encodedPassword, matches,
            adminUser.getPassword(), adminMatches
        );
    }
}