package com.upsilon.TCMP.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebMvcConfig.class);


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        System.out.println("=== Configuring View Controllers ===");
        
        // Home pages
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/index").setViewName("home");
        
        System.out.println("Home page mappings configured");
        
        // Authentication pages
        registry.addViewController("/login").setViewName("auth/login");
        registry.addViewController("/register").setViewName("auth/register");
        registry.addViewController("/verify-email").setViewName("auth/verify");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        log.info("Configuring resource handlers");
        
        // Configure template resources
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("classpath:/templates/")
                .setCachePeriod(3600)
                .resourceChain(true);
        log.info("Template resources configured");

        // Configure static resources
        // Static resources
        registry.addResourceHandler("/static/**", "/css/**", "/js/**", "/images/**")
                .addResourceLocations("classpath:/static/", "file:src/main/resources/static/")
                .setCachePeriod(3600)
                .resourceChain(true);
        log.info("Static resources configured");
        
        // Additional debug information
        log.info("Resource handlers configuration completed");
        log.info("Template path: classpath:/templates/");
        log.info("Static path: classpath:/static/");
    }
}