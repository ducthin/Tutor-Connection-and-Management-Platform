package com.upsilon.TCMP;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String rawPassword = "admin123";
        // This is the hash we're now using in data.sql
        String storedHash = "$2a$10$FvHQqZRtIGsXvUCQn39XmeqyX56WeuIJhgGU0v5tf74ICcSoOPqYi";
        
        System.out.println("Testing current database hash:");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Stored hash: " + storedHash);
        boolean matches = encoder.matches(rawPassword, storedHash);
        System.out.println("Password matches: " + matches);
        
        if (!matches) {
            System.out.println("\nGenerating new hash for verification:");
            String newHash = encoder.encode(rawPassword);
            System.out.println("New hash: " + newHash);
            System.out.println("New hash verifies: " + encoder.matches(rawPassword, newHash));
            
            // Test variations to ensure encoding is consistent
            System.out.println("\nTesting password variations:");
            System.out.println("'Admin123' matches: " + encoder.matches("Admin123", storedHash));
            System.out.println("'admin1234' matches: " + encoder.matches("admin1234", storedHash));
            System.out.println("'admin 123' matches: " + encoder.matches("admin 123", storedHash));
        }
        
        System.out.println("\nGenerating additional test hashes:");
        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(rawPassword);
            boolean valid = encoder.matches(rawPassword, hash);
            System.out.println(String.format("Hash %d: %s (Valid: %s)", i, hash, valid));
        }
    }
}