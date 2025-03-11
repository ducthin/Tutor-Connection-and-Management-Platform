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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    @Primary
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        logger.debug("Created BCryptPasswordEncoder with strength 10");
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
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationProvider authProvider) throws Exception {

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/test/**")
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
                        "/test/**",
                        "/error"
                    ).permitAll()
                    .requestMatchers("/dashboard/**").authenticated()
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers("/tutor/**").hasAuthority("ROLE_TUTOR")
                    .requestMatchers("/student/**").hasAuthority("ROLE_STUDENT")
                    .requestMatchers("/tutors", "/tutors/search", "/tutors/results", "/tutors/available", "/subjects").permitAll()
                    .requestMatchers("/tutors/profile/**", "/tutors/redirect-profile/**").permitAll()
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
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}