package com.upsilon.TCMP.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DebugAuthenticationProvider implements AuthenticationProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugAuthenticationProvider.class);
    
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public DebugAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        logger.debug("Attempting authentication for user: {}", username);
        logger.debug("Raw password length: {}", password.length());

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        logger.debug("Found user details:");
        logger.debug("Username: {}", userDetails.getUsername());
        logger.debug("Stored password hash: {}", userDetails.getPassword());
        logger.debug("Authorities: {}", userDetails.getAuthorities());

        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            logger.debug("Password verification successful");
            return new UsernamePasswordAuthenticationToken(
                userDetails, password, userDetails.getAuthorities());
        }

        logger.error("Password verification failed");
        logger.debug("Raw password: {}", password);
        logger.debug("Stored hash: {}", userDetails.getPassword());
        throw new BadCredentialsException("Invalid password");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}