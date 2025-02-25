package com.upsilon.TCMP.config;

import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for email: {}", username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        // Debug user details
        logger.debug("Found user: id={}, email={}, active={}, verified={}", 
            user.getId(), user.getEmail(), user.isActive(), user.isEmailVerified());
        logger.debug("User password hash: {}", user.getPassword());
        logger.debug("User role: {}", user.getRole());

        // Check if user exists and is active
        if (!user.isActive()) {
            logger.error("User account is deactivated: {}", username);
            throw new UsernameNotFoundException("Account is deactivated");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            logger.error("User email is not verified: {}", username);
            throw new UsernameNotFoundException("Please verify your email first");
        }

        String roleName = user.getRole().name();
        logger.debug("Building UserDetails with role: {}", roleName);

        // Create UserDetails object
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singleton(new SimpleGrantedAuthority(roleName)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        logger.debug("Created UserDetails for: {}", username);
        logger.debug("UserDetails password hash: {}", userDetails.getPassword());
        logger.debug("UserDetails authorities: {}", userDetails.getAuthorities());

        return userDetails;
    }
}