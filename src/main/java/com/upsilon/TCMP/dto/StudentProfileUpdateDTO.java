package com.upsilon.TCMP.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentProfileUpdateDTO {
    // User fields
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;
    
    // Student fields
    @Size(max = 500, message = "Learning preferences must not exceed 500 characters")
    private String learningPreferences;

    @Size(max = 500, message = "Subjects of interest must not exceed 500 characters")
    private String subjectsOfInterest;
}