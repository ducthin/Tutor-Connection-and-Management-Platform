package com.upsilon.TCMP.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/test")
public class TestController {

    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private static final String STORED_HASH = "$2a$10$nFdqF7dC/6OBSp2yDU8dUe53B8JYaHVOyqk9E4i3yGarDw8uSws96";

    public TestController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/encode")
    public String testEncode(@RequestParam(defaultValue = "admin123") String password) {
        String hash = passwordEncoder.encode(password);
        logger.debug("Generated hash for '{}': {}", password, hash);
        boolean matches = passwordEncoder.matches(password, hash);
        logger.debug("Verifying newly generated hash: {}", matches);
        return String.format("""
            Password: %s
            Generated hash: %s
            Verified: %s
            """, 
            password, hash, matches);
    }

    @GetMapping("/verify")
    public String testVerify(
            @RequestParam(defaultValue = "admin123") String password,
            @RequestParam(defaultValue = STORED_HASH) String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        logger.debug("Verifying password '{}' against hash: {}", password, hash);
        logger.debug("Result: {}", matches);
        return String.format("""
            Password: %s
            Hash: %s
            Matches: %s
            """,
            password, hash, matches);
    }

    @GetMapping("/verify-stored")
    public String testVerifyStored(@RequestParam(defaultValue = "admin123") String password) {
        boolean matches = passwordEncoder.matches(password, STORED_HASH);
        logger.debug("Verifying password '{}' against stored hash", password);
        logger.debug("Stored hash: {}", STORED_HASH);
        logger.debug("Result: {}", matches);
        
        // Generate new hash for comparison
        String newHash = passwordEncoder.encode(password);
        boolean newMatches = passwordEncoder.matches(password, newHash);
        
        return String.format("""
            Testing with stored hash:
            Password: %s
            Stored hash: %s
            Matches: %s
            
            Testing with new hash:
            New hash: %s
            New hash matches: %s
            """,
            password, STORED_HASH, matches,
            newHash, newMatches);
    }
}