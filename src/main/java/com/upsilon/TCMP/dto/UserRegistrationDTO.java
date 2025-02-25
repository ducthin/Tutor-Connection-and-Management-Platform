package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(regexp = "(^$|^[0-9]{10}$)", message = "Phone number must be 10 digits")
    private String phone;

    private Role role = Role.ROLE_STUDENT; // Default role

    public void setRole(String role) {
        try {
            this.role = Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            // Default to ROLE_STUDENT if invalid role is provided
            this.role = Role.ROLE_STUDENT;
        }
    }

    public String getRole() {
        return role != null ? role.name() : Role.ROLE_STUDENT.name();
    }

    // Helper method to get the Role enum directly
    public Role getRoleEnum() {
        return role;
    }
}