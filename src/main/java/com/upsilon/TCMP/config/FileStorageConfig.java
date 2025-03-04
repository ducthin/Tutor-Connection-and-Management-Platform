package com.upsilon.TCMP.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:${user.home}/tcmp/uploads}")
    private String baseUploadDir;
    
    private String profilePicturesDir;
    
    @PostConstruct
    public void init() {
        try {
            profilePicturesDir = Paths.get(baseUploadDir, "profile-pictures").toString();
            Files.createDirectories(Paths.get(profilePicturesDir));
            System.out.println("Created upload directory: " + profilePicturesDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        String uploadPath = "file:" + baseUploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    public String getProfilePicturesDir() {
        return profilePicturesDir;
    }

    public String getUploadPathForWeb(String filename) {
        return "/uploads/profile-pictures/" + filename;
    }
}