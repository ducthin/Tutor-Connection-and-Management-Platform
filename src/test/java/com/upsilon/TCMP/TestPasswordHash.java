package com.upsilon.TCMP;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordHash {
    public static void main(String[] args) {
        // Create BCryptPasswordEncoder with default strength (10)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // The password we're testing
        String rawPassword = "admin123";
        
        // Generate and print multiple hashes to see the pattern
        System.out.println("Generating multiple hashes for 'admin123':");
        for (int i = 0; i < 5; i++) {
            String hash = encoder.encode(rawPassword);
            System.out.println("Hash " + (i + 1) + ": " + hash);
            System.out.println("Verifies: " + encoder.matches(rawPassword, hash));
        }
        
        // Test against stored hash
        String storedHash = "$2a$10$8.RvzAOQxcRxQoN4kaFZ0.6LAhHClUXg8SEfyW4MLns0p8FBlQgEe";
        System.out.println("\nTesting stored hash from database:");
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Raw password ('admin123') matches stored hash: " + encoder.matches(rawPassword, storedHash));
        
        // Test some variations to verify encoding behavior
        System.out.println("\nTesting password variations:");
        System.out.println("'Admin123' matches: " + encoder.matches("Admin123", storedHash));
        System.out.println("'admin1234' matches: " + encoder.matches("admin1234", storedHash));
        System.out.println("'admin 123' matches: " + encoder.matches("admin 123", storedHash));
    }
}