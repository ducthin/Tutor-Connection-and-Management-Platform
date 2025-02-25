package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private String bio;
    private Role role;
    private boolean active;
    private boolean emailVerified;
    private String token;
}