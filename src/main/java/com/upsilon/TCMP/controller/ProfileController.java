package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.Role;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.List;


@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final StudentService studentService;
    private final TutorService tutorService;

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        try {
            UserDTO user = userService.getUserByEmail(authentication.getName());
            model.addAttribute("user", user);

            if (user.getRole() == Role.ROLE_STUDENT) {
                StudentDTO student = studentService.getStudentByUserId(user.getId());
                model.addAttribute("student", student);
                return "profile/student";
            } else if (user.getRole() == Role.ROLE_TUTOR) {
                TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
                model.addAttribute("tutor", tutor);
                model.addAttribute("isTutorProfile", true);
                
                // Add tutor subjects and availability
                List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutor.getId());
                List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutor.getId());
                List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutor.getId(), 5);
                Double averageRating = tutorService.getAverageRating(tutor.getId());

                model.addAttribute("tutorSubjects", tutorSubjects);
                model.addAttribute("availability", availability);
                model.addAttribute("recentReviews", recentReviews);
                model.addAttribute("averageRating", averageRating);

                // Debug logging
                System.out.println("Tutor subjects: " + (tutorSubjects != null ? tutorSubjects.size() : "null"));
                System.out.println("Availability slots: " + (availability != null ? availability.size() : "null"));
                
                return "profile/tutor";
            }

            return "profile/index";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
            return "profile/index";
        }
    }

    @GetMapping("/edit")
    public String editProfile(Model model, Authentication authentication) {
        try {
            UserDTO user = userService.getUserByEmail(authentication.getName());
            model.addAttribute("user", user);

            if (user.getRole() == Role.ROLE_STUDENT) {
                StudentDTO student = studentService.getStudentByUserId(user.getId());
                StudentProfileUpdateDTO updateDTO = new StudentProfileUpdateDTO();
                updateDTO.setFullName(user.getFullName());
                updateDTO.setPhoneNumber(user.getPhoneNumber());
                updateDTO.setLearningPreferences(student.getLearningPreferences());
                updateDTO.setSubjectsOfInterest(student.getSubjectsOfInterest());
                
                model.addAttribute("studentProfileUpdateDTO", updateDTO);
                model.addAttribute("student", student);
                return "profile/edit-student";
            } else if (user.getRole() == Role.ROLE_TUTOR) {
                TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
                TutorProfileUpdateDTO updateDTO = new TutorProfileUpdateDTO();
                updateDTO.setFullName(user.getFullName());
                updateDTO.setPhoneNumber(user.getPhoneNumber());
                updateDTO.setBio(tutor.getBio());
                updateDTO.setQualifications(tutor.getQualifications());
                updateDTO.setSubjectsTaught(tutor.getSubjectsTaught());
                updateDTO.setHourlyRate(tutor.getHourlyRate());

                model.addAttribute("tutorProfileUpdateDTO", updateDTO);
                model.addAttribute("tutor", tutor);
                model.addAttribute("user", user);
                return "profile/edit-tutor";
            }

            return "profile/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
            return "profile/edit";
        }
    }

    @PostMapping("/edit/student")
    public String updateStudentProfile(
            @ModelAttribute("studentProfileUpdateDTO") @Valid StudentProfileUpdateDTO updateDTO,
            BindingResult bindingResult,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            UserDTO user = userService.getUserByEmail(authentication.getName());
            StudentDTO studentDTO = studentService.getStudentByUserId(user.getId());

            // Add current user data to model in case of validation failure
            model.addAttribute("user", user);
            model.addAttribute("student", studentDTO);

            if (bindingResult.hasErrors()) {
                return "profile/edit-student";
            }
            
            try {
                // Create updated student DTO
                StudentDTO updatedStudent = studentDTO;
                updatedStudent.setLearningPreferences(updateDTO.getLearningPreferences());
                updatedStudent.setSubjectsOfInterest(updateDTO.getSubjectsOfInterest());
                
                // Set user info in student DTO
                UserDTO updatedUser = new UserDTO();
                updatedUser.setId(user.getId());
                updatedUser.setFullName(updateDTO.getFullName());
                updatedUser.setPhoneNumber(updateDTO.getPhoneNumber());
                updatedUser.setProfilePictureUrl(user.getProfilePictureUrl());
                updatedStudent.setUser(updatedUser);
                
                // Update student with both student and user data
                studentService.updateStudent(updatedStudent);
            } catch (Exception e) {
                model.addAttribute("error", "Failed to update profile: " + e.getMessage());
                return "profile/edit-student";
            }

            // Handle profile picture upload
            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    userService.uploadProfilePicture(user.getId(), profilePicture);
                } catch (RuntimeException e) {
                    model.addAttribute("user", user);
                    model.addAttribute("error", "Failed to upload profile picture: " + e.getMessage());
                    model.addAttribute("studentProfileUpdateDTO", updateDTO);
                    model.addAttribute("student", studentDTO);
                    return "profile/edit-student";
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/edit/tutor")
    public String updateTutorProfile(
            @Valid @ModelAttribute TutorProfileUpdateDTO updateDTO,
            BindingResult bindingResult,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            UserDTO user = userService.getUserByEmail(authentication.getName());
            TutorDTO tutor = tutorService.getTutorByUserId(user.getId());

            // Add current data to model in case of validation failure
            model.addAttribute("user", user);
            model.addAttribute("tutor", tutor);

            if (bindingResult.hasErrors()) {
                return "profile/edit-tutor";
            }

            // Validate profile picture if provided
            if (profilePicture != null && !profilePicture.isEmpty()) {
                if (profilePicture.getSize() > 5 * 1024 * 1024) {
                    model.addAttribute("uploadError", "File size must be less than 5MB");
                    return "profile/edit-tutor";
                }
                String contentType = profilePicture.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    model.addAttribute("uploadError", "Only image files are allowed");
                    return "profile/edit-tutor";
                }
            }
            
            // Create updated tutor DTO
            tutorService.updateTutorProfile(tutor.getId(), updateDTO);
            
            // Handle profile picture upload after profile update
            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    userService.uploadProfilePicture(user.getId(), profilePicture);
                } catch (RuntimeException e) {
                    model.addAttribute("uploadError", "Failed to upload profile picture: " + e.getMessage());
                    return "profile/edit-tutor";
                }
            }

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}