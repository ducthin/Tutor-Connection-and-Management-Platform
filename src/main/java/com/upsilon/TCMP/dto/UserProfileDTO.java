package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Student-specific fields
    private String learningPreferences;
    private String subjectsOfInterest;
    
    // Tutor-specific fields
    private String qualifications;
    private String bio;
    private Double rating;
    private Double hourlyRate;
    private Boolean isVerified;
    
    // Calculated fields
    private Long totalSessions;
    private Long completedSessions;
    private Double totalEarnings;    // For tutors
    private Double averageRating;    // For tutors
    private Integer reviewCount;     // For tutors
}