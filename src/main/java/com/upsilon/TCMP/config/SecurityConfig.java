package com.upsilon.TCMP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(
        JwtTokenProvider jwtTokenProvider, 
        CustomUserDetailsService userDetailsService,
        CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Bean
    @Primary // Ensure this is the primary PasswordEncoder used throughout the application
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        logger.debug("Created BCryptPasswordEncoder with strength 10");
        
        // Test the encoder
        String testPassword = "admin123";
        String testHash = "$2a$10$FvHQqZRtIGsXvUCQn39XmeqyX56WeuIJhgGU0v5tf74ICcSoOPqYi";
        boolean matches = encoder.matches(testPassword, testHash);
        logger.debug("Test password verification result: {}", matches);
        
        return encoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CustomAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        CustomAuthenticationProvider provider = new CustomAuthenticationProvider(userDetailsService, passwordEncoder);
        logger.debug("Configured CustomAuthenticationProvider with UserDetailsService and PasswordEncoder");
        return provider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationProvider authProvider) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")  // Disable CSRF for API endpoints
                .ignoringRequestMatchers("/test/**") // Disable for test endpoints
            )
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/images/profiles/**",
                        "/static/**",
                        "/webjars/**",
                        "/fonts/**",
                        "/favicon.ico"
                    ).permitAll()
                    .requestMatchers(
                        "/",
                        "/home",
                        "/index",
                        "/login",
                        "/register",
                        "/verify",
                        "/verify/resend",
                        "/forgot-password",
                        "/reset-password",
                        "/api/auth/**",
                        "/api/public/**",
                        "/test/**",  // Allow access to all test endpoints
                        "/error"
                    ).permitAll()
                    .requestMatchers("/dashboard/**").authenticated()
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers("/tutor/**").hasAuthority("ROLE_TUTOR")
                    .requestMatchers("/student/**").hasAuthority("ROLE_STUDENT")
                    .requestMatchers("/tutors", "/tutors/search", "/tutors/results", "/tutors/available", "/subjects").permitAll()
                    .requestMatchers("/tutors/favorites/**").hasAuthority("ROLE_STUDENT")
                    .requestMatchers("/api/tutors/**").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .failureHandler(authenticationSuccessHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(rememberMe -> rememberMe
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authenticationProvider(authProvider);

        return http.build();
    }
}