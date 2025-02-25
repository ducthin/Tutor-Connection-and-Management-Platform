package com.upsilon.TCMP.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        super();
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
        setHideUserNotFoundExceptions(false);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            logger.debug("Starting authentication for user: {}", authentication.getName());
            Authentication result = super.authenticate(authentication);
            logger.debug("Authentication successful for user: {}", authentication.getName());
            return result;
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", authentication.getName(), e);
            throw e;
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, 
                                                UsernamePasswordAuthenticationToken authentication) 
                                                throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.error("Authentication failed: no credentials provided");
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials",
                "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();
        String storedPassword = userDetails.getPassword();

        logger.debug("Attempting password verification for user: {}", userDetails.getUsername());
        logger.debug("Stored password hash: {}", storedPassword);
        logger.debug("Raw password length: {}", presentedPassword.length());

        if (!getPasswordEncoder().matches(presentedPassword, storedPassword)) {
            logger.error("Authentication failed: password does not match stored value");
            
            // Generate a new hash of the presented password for comparison
            String newHash = getPasswordEncoder().encode(presentedPassword);
            logger.debug("Raw password produces hash: {}", newHash);
            logger.debug("Stored hash was: {}", storedPassword);
            
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials",
                "Bad credentials"));
        }

        logger.debug("Password verification successful for user: {}", userDetails.getUsername());
    }
}