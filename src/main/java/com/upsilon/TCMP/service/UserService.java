package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.UserDTO;
import com.upsilon.TCMP.enums.Role;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    
    UserDTO getUserById(Integer id);
    
    UserDTO getUserByEmail(String email);
    
    List<UserDTO> getAllUsers();
    
    void updateUser(UserDTO userDTO);
    
    void deleteUser(Integer id);
    
    boolean existsByEmail(String email);
    
    void activateUser(String token);
    
    void resetPassword(String token, String newPassword);
    
    void requestPasswordReset(String email);
    
    // Admin dashboard methods
    long countAllUsers();
    
    long countUsersByRoleAndStatus(Role role, boolean isActive);
    
    List<UserDTO> findUsersByRole(Role role);
    
    List<UserDTO> findActiveUsers();
    
    List<UserDTO> findInactiveUsers();
    
    void setUserStatus(Integer userId, boolean active);

    // Profile picture management
    String uploadProfilePicture(Integer userId, MultipartFile file);
}