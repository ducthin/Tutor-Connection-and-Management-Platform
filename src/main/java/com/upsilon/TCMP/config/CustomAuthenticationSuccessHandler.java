package com.upsilon.TCMP.config;

import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler 
        implements AuthenticationFailureHandler {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Set default target URL based on role
        String targetUrl = switch (user.getRole().name()) {
            case "ROLE_ADMIN" -> "/dashboard/admin";
            case "ROLE_TUTOR" -> "/dashboard/tutor";
            case "ROLE_STUDENT" -> "/dashboard/student";
            default -> "/dashboard";
        };

        logger.debug("Setting target URL for user {} to: {}", email, targetUrl);
        setDefaultTargetUrl(targetUrl);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      AuthenticationException exception) 
                                      throws IOException, ServletException {
        
        String email = request.getParameter("username");
        logger.error("Authentication failed", exception);

        String errorMessage = switch (exception.getClass().getSimpleName()) {
            case "BadCredentialsException" -> {
                logger.debug("Invalid credentials attempt for email: {}", email);
                yield "Invalid email or password";
            }
            case "DisabledException" -> {
                logger.debug("Disabled account access attempt for email: {}", email);
                yield "Please verify your email first";
            }
            case "LockedException" -> {
                logger.debug("Locked account access attempt for email: {}", email);
                yield "Your account has been deactivated";
            }
            case "UsernameNotFoundException" -> {
                logger.debug("Non-existent account access attempt for email: {}", email);
                yield "Invalid email or password";
            }
            default -> {
                String message = exception.getMessage();
                if (exception.getCause() != null && message != null && message.contains("No enum constant")) {
                    logger.error("Role configuration error for email: {}", email);
                    yield "Invalid role assignment. Please contact support.";
                }
                logger.error("Unexpected authentication error", exception);
                yield "An error occurred during login. Please try again.";
            }
        };

        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
        String encodedEmail = email != null ? URLEncoder.encode(email, StandardCharsets.UTF_8.toString()) : "";
        
        response.sendRedirect("/login?error=true&message=" + encodedError + "&username=" + encodedEmail);
    }
}