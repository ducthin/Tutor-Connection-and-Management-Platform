package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.enums.Role;
import com.upsilon.TCMP.repository.UserRepository;
import com.upsilon.TCMP.service.UserService;
import com.upsilon.TCMP.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private  FileStorageConfig fileStorageConfig;


    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setActive(true);
        user.setEmailVerified(false);
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setBio(userDTO.getBio());
        
        return convertToDTO(userRepository.save(user));
    }

    @Override
    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setBio(userDTO.getBio());
        
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void activateUser(String token) {
        // Implementation for activating user with token
        // This would typically involve verifying the token and updating the user's status
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Implementation for resetting password with token
        // This would typically involve verifying the token and updating the user's password
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Implementation for generating and sending password reset token
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public long countUsersByRoleAndStatus(Role role, boolean isActive) {
        return userRepository.countByRoleAndActive(role, isActive);
    }

    @Override
    public List<UserDTO> findUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findActiveUsers() {
        return userRepository.findByActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findInactiveUsers() {
        return userRepository.findByActive(false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void setUserStatus(Integer userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    public String uploadProfilePicture(Integer userId, MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Please upload a file");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File size should be less than 5MB");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Clean up old profile picture if it exists
            String oldProfilePicture = user.getProfilePictureUrl();
            if (oldProfilePicture != null && !oldProfilePicture.isEmpty()) {
                try {
                    String oldFileName = oldProfilePicture.substring(oldProfilePicture.lastIndexOf('/') + 1);
                    Path oldFilePath = Paths.get(fileStorageConfig.getProfilePicturesDir(), oldFileName);
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Could not delete old profile picture: " + e.getMessage());
                }
            }

            // Generate a unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;
            
            // Save the file to the profile pictures directory
            Path filePath = Paths.get(fileStorageConfig.getProfilePicturesDir(), filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Get the web-accessible URL path
            String profilePictureUrl = fileStorageConfig.getUploadPathForWeb(filename);
            user.setProfilePictureUrl(profilePictureUrl);
            userRepository.save(user);
            
            return profilePictureUrl;
        } catch (IOException ex) {
            throw new RuntimeException("Could not upload profile picture", ex);
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setBio(user.getBio());
        return dto;
    }
}