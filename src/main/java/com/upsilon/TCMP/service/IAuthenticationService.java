package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.UserDTO;
import com.upsilon.TCMP.dto.UserLoginDTO;
import com.upsilon.TCMP.dto.UserRegistrationDTO;

public interface IAuthenticationService {
    UserDTO login(UserLoginDTO loginDTO);
    void register(UserRegistrationDTO registrationDTO);
    void logout(Integer userId);
    void verifyEmail(String token);
    void sendVerificationEmail(String email);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String password, String confirmPassword);
    void changePassword(Integer userId, String oldPassword, String newPassword);
    String generateJwtToken(UserDTO userDTO);
    boolean validateToken(String token);
    void revokeToken(String token);
    String refreshToken(String token);
}