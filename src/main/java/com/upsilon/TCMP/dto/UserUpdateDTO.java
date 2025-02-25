package com.upsilon.TCMP.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl;
}