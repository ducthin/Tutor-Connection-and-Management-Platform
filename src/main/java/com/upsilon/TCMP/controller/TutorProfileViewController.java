package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import java.util.stream.Collectors;
import com.upsilon.TCMP.entity.TutorSubject;
import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.enums.DayOfWeek;
import com.upsilon.TCMP.enums.Role;
import com.upsilon.TCMP.repository.TutorSubjectRepository;
import com.upsilon.TCMP.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import com.upsilon.TCMP.service.SubjectService;
import com.upsilon.TCMP.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.EnumMap;
import java.util.stream.Collectors;
import com.upsilon.TCMP.enums.DayOfWeek;

@Controller
@RequestMapping("/tutor")
@PreAuthorize("hasRole('TUTOR')")
public class TutorProfileViewController {

    @Autowired
    private TutorSubjectRepository tutorSubjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        try {
            System.out.println("=== Profile Page Access ===");
            System.out.println("Auth Principal: " + auth.getPrincipal());
            System.out.println("Auth Name: " + auth.getName());
            System.out.println("Auth Authorities: " + auth.getAuthorities());
            
            Integer tutorId = getTutorIdFromAuth(auth);
            System.out.println("Retrieved Tutor ID: " + tutorId);
            
            TutorDTO tutor = tutorService.getTutorById(tutorId);
            System.out.println("Retrieved Tutor: " + tutor.getUser().getFullName());
            
            System.out.println("Attempting to retrieve subjects directly from repository for tutor ID: " + tutorId);
            List<TutorSubject> rawSubjects = tutorSubjectRepository.findByTutorId(tutorId);
            System.out.println("Direct repository query found " + (rawSubjects != null ? rawSubjects.size() : "null") + " subjects");
            if (rawSubjects != null && !rawSubjects.isEmpty()) {
                rawSubjects.forEach(subject -> {
                    System.out.println("Raw subject - ID: " + subject.getId() +
                                     ", Subject: " + (subject.getSubject() != null ? subject.getSubject().getName() : "null") +
                                     ", Rate: " + subject.getHourlyRate() +
                                     ", Active: " + subject.isActive());
                });
            }
            
            List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
            System.out.println("Retrieved Subjects from service: " + (tutorSubjects != null ? tutorSubjects.size() : "null"));
            
            if (tutorSubjects != null && !tutorSubjects.isEmpty()) {
                System.out.println("Subjects details:");
                tutorSubjects.forEach(subject -> {
                    System.out.println("  - Subject: " + subject.getSubjectName());
                    System.out.println("    Rate: " + subject.getHourlyRate());
                    System.out.println("    Active: " + subject.isActive());
                    System.out.println("    Experience: " + subject.getExperienceYears() + " years");
                });
            } else {
                System.out.println("No subjects found for tutor");
            }
            
            List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
            List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutorId, 5);
            
            model.addAttribute("tutor", tutor);
            model.addAttribute("tutorSubjects", tutorSubjects);
            model.addAttribute("availability", availability);
            model.addAttribute("recentReviews", recentReviews);
            
            System.out.println("=== Profile Page Data Added to Model ===");
            return "profile/tutor";
            
        } catch (Exception e) {
            System.err.println("Error in profile page:");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication auth, Model model) {
        Integer tutorId = getTutorIdFromAuth(auth);
        TutorDTO tutor = tutorService.getTutorById(tutorId);
        model.addAttribute("tutor", tutor);
        return "profile/edit-tutor";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            Authentication auth,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String qualifications,
            @RequestParam(required = false) String subjectsTaught,
            @RequestParam(required = false) BigDecimal hourlyRate,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        
        Integer tutorId = getTutorIdFromAuth(auth);
        TutorProfileUpdateDTO updateDTO = new TutorProfileUpdateDTO();
        updateDTO.setFullName(fullName);
        updateDTO.setPhoneNumber(phoneNumber);
        updateDTO.setBio(bio);
        updateDTO.setQualifications(qualifications);
        updateDTO.setSubjectsTaught(subjectsTaught);
        if (hourlyRate != null) {
            updateDTO.setHourlyRate(hourlyRate.doubleValue());
        }
        
        tutorService.updateTutorProfile(tutorId, updateDTO);

        // Handle profile picture upload
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                tutorService.uploadProfilePicture(tutorId, profilePicture);
            } catch (Exception e) {
                // Log the error but don't fail the entire update
                System.err.println("Failed to upload profile picture: " + e.getMessage());
            }
        }

        return "redirect:/tutor/profile";
    }

    @GetMapping("/subjects")
    public String subjects(Authentication auth, Model model) {
        try {
            System.out.println("=== Subjects Page Access ===");
            Integer tutorId = getTutorIdFromAuth(auth);
            System.out.println("Got tutor ID: " + tutorId);
            
            // Get raw subjects from repository for debugging
            List<TutorSubject> rawSubjects = tutorSubjectRepository.findByTutorId(tutorId);
            System.out.println("Raw subjects found: " + (rawSubjects != null ? rawSubjects.size() : "null"));
            if (rawSubjects != null) {
                rawSubjects.forEach(subject -> {
                    System.out.println("Raw subject: " + subject.getId() +
                                     ", Name: " + (subject.getSubject() != null ? subject.getSubject().getName() : "null") +
                                     ", Rate: " + subject.getHourlyRate());
                });
            }

            // Get subjects through service
            List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
            System.out.println("Service subjects found: " + (tutorSubjects != null ? tutorSubjects.size() : "null"));
            
            List<SubjectDTO> allSubjects = subjectService.getAllSubjects();
            System.out.println("All available subjects: " + (allSubjects != null ? allSubjects.size() : "null"));
            
            model.addAttribute("tutorSubjects", tutorSubjects);
            model.addAttribute("allSubjects", allSubjects);
            
            System.out.println("=== Subjects Page Data Added to Model ===");
            return "tutor/subjects";
        } catch (Exception e) {
            System.err.println("Error in subjects page: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/subjects/add")
    public String addSubject(
            Authentication auth,
            @Valid @ModelAttribute TutorSubjectCreateDTO createDTO,
            BindingResult bindingResult,
            Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
                return subjects(auth, model);
            }
            Integer tutorId = getTutorIdFromAuth(auth);
            createDTO.setTutorId(tutorId);
            tutorService.addSubject(tutorId, createDTO);
            model.addAttribute("success", "Subject added successfully");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tutor/subjects";
    }

    @GetMapping("/subjects/add")
    public String showAddSubjectForm(Authentication auth, Model model) {
        try {
            Integer tutorId = getTutorIdFromAuth(auth);
            
            // Get all available subjects
            List<SubjectDTO> availableSubjects = subjectService.getAllSubjects().stream()
                .filter(subject -> tutorService.getTutorSubjects(tutorId).stream()
                    .noneMatch(ts -> ts.getSubjectId().equals(subject.getId())))
                .collect(Collectors.toList());
            
            model.addAttribute("allSubjects", availableSubjects);
            return "tutor/add-subject";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tutor/subjects";
        }
    }

    @GetMapping("/subjects/{id}/edit")
    public String showEditSubjectForm(Authentication auth, @PathVariable("id") Integer tutorSubjectId, Model model) {
        try {
            System.out.println("Showing edit form for TutorSubject ID: " + tutorSubjectId);
            Integer tutorId = getTutorIdFromAuth(auth);
            System.out.println("Verified tutor ID: " + tutorId);

            // Get the subject
            TutorSubject subject = tutorSubjectRepository.findById(tutorSubjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
            System.out.println("Found TutorSubject with ID: " + subject.getId());

            // Verify ownership
            if (!subject.getTutor().getId().equals(tutorId)) {
                System.err.println("Authorization failed - Subject belongs to tutor " +
                                 subject.getTutor().getId() + ", not " + tutorId);
                throw new AccessDeniedException("Not authorized to edit this subject");
            }
            System.out.println("Authorization verified for edit");

            // Convert to DTO
            TutorSubjectDTO subjectDTO = new TutorSubjectDTO();
            subjectDTO.setId(subject.getId());
            subjectDTO.setSubjectId(subject.getSubject().getId());
            subjectDTO.setSubjectName(subject.getSubject().getName());
            subjectDTO.setHourlyRate(subject.getHourlyRate());
            subjectDTO.setExperienceYears(subject.getExperienceYears());
            subjectDTO.setDescription(subject.getDescription());
            subjectDTO.setActive(subject.isActive());

            System.out.println("Created DTO for edit form:");
            System.out.println("- ID: " + subjectDTO.getId());
            System.out.println("- Subject: " + subjectDTO.getSubjectName());
            System.out.println("- Rate: " + subjectDTO.getHourlyRate());
            System.out.println("- Experience: " + subjectDTO.getExperienceYears());
            System.out.println("- Active: " + subjectDTO.isActive());

            model.addAttribute("subject", subjectDTO);
            System.out.println("Added subject DTO to model, rendering edit form");
            return "tutor/edit-subject";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tutor/subjects";
        }
    }

    @PostMapping("/subjects/{id}/update")
    public String updateSubject(
            Authentication auth,
            @PathVariable("id") Integer tutorSubjectId,
            @ModelAttribute TutorSubjectUpdateDTO updateDTO,
            Model model) {
        try {
            System.out.println("Attempting to update TutorSubject with ID: " + tutorSubjectId);
            System.out.println("Update data: rate=" + updateDTO.getRate() +
                             ", expYears=" + updateDTO.getExperienceYears() +
                             ", active=" + updateDTO.getActive());
            
            Integer tutorId = getTutorIdFromAuth(auth);
            System.out.println("Verified tutor ID: " + tutorId);
            
            tutorService.updateSubject(tutorId, tutorSubjectId, updateDTO);
            System.out.println("Successfully updated TutorSubject: " + tutorSubjectId);
            
            model.addAttribute("success", "Subject updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating TutorSubject: " + tutorSubjectId);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tutor/subjects";
    }

    @PostMapping("/subjects/{id}/delete")
    public String deleteSubject(
            Authentication auth,
            @PathVariable("id") Integer tutorSubjectId,
            Model model) {
        try {
            System.out.println("Attempting to delete TutorSubject with ID: " + tutorSubjectId);
            Integer tutorId = getTutorIdFromAuth(auth);
            System.out.println("Verified tutor ID: " + tutorId);
            
            tutorService.removeSubject(tutorId, tutorSubjectId);
            System.out.println("Successfully deleted TutorSubject: " + tutorSubjectId);
            
            model.addAttribute("success", "Subject removed successfully");
        } catch (Exception e) {
            System.err.println("Error deleting TutorSubject: " + tutorSubjectId);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tutor/subjects";
    }

    @GetMapping("/availability")
    public String availability(Authentication auth, Model model) {
        try {
            Integer tutorId = getTutorIdFromAuth(auth);
            List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
            
            // Generate time slots (8 AM to 8 PM)
            List<String> timeSlots = new ArrayList<>();
            LocalTime time = LocalTime.of(8, 0);
            while (!time.isAfter(LocalTime.of(20, 0))) {
                timeSlots.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                time = time.plusHours(1);
            }
            
            // Create availability matrix
            Map<DayOfWeek, Map<String, TutorAvailabilityDTO>> availabilityMatrix = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek day : DayOfWeek.values()) {
                availabilityMatrix.put(day, new HashMap<>());
            }
            
            // Fill matrix with existing availabilities
            for (TutorAvailabilityDTO slot : availability) {
                availabilityMatrix.get(slot.getDayOfWeek())
                    .put(slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")), slot);
            }
            
            model.addAttribute("timeSlots", timeSlots);
            model.addAttribute("days", DayOfWeek.values());
            model.addAttribute("availabilityMatrix", availabilityMatrix);
            
            return "tutor/availability";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading availability: " + e.getMessage());
            return "tutor/availability";
        }
    }

    @PostMapping("/availability/add")
    public String addAvailability(
            Authentication auth,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "false") boolean isRecurring,
            Model model) {
        try {
            Integer tutorId = getTutorIdFromAuth(auth);
            
            TutorAvailabilityCreateDTO createDTO = new TutorAvailabilityCreateDTO();
            createDTO.setTutorId(tutorId);
            createDTO.setDayOfWeek(dayOfWeek);
            createDTO.setStartTime(LocalTime.parse(startTime));
            createDTO.setEndTime(LocalTime.parse(endTime));
            createDTO.setIsRecurring(isRecurring);
            
            tutorService.addAvailability(createDTO);
            model.addAttribute("success", "Availability added successfully");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tutor/availability";
    }

    @PostMapping("/availability/{id}/delete")
    public String deleteAvailability(
            Authentication auth,
            @PathVariable("id") Integer availabilityId,
            Model model) {
        try {
            tutorService.removeAvailability(availabilityId);
            model.addAttribute("success", "Time slot removed successfully");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tutor/availability";
    }

    @GetMapping("/sessions")
    public String sessions(Authentication auth, Model model) {
        Integer tutorId = getTutorIdFromAuth(auth);
        List<SessionDTO> sessions = tutorService.getTutorSessions(tutorId);
        model.addAttribute("sessions", sessions);
        return "tutor/sessions";
    }

    @GetMapping("/reviews")
    public String reviews(Authentication auth, Model model) {
        Integer tutorId = getTutorIdFromAuth(auth);
        List<ReviewDTO> reviews = tutorService.getTutorReviews(tutorId);
        Double averageRating = tutorService.getAverageRating(tutorId);
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        return "tutor/reviews";
    }

    @GetMapping("/earnings")
    public String earnings(
            Authentication auth,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        Integer tutorId = getTutorIdFromAuth(auth);
        
        if (year == null) year = LocalDateTime.now().getYear();
        if (month == null) month = LocalDateTime.now().getMonthValue();
        
        Double totalEarnings = tutorService.calculateTotalEarnings(tutorId);
        Double monthlyEarnings = tutorService.calculateMonthlyEarnings(tutorId, year, month);
        
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("monthlyEarnings", monthlyEarnings);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime date = now.minusMonths(i);
            Double earnings = tutorService.calculateMonthlyEarnings(
                tutorId, date.getYear(), date.getMonthValue());
            Map<String, Object> data = new HashMap<>();
            data.put("month", date.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            data.put("earnings", earnings);
            monthlyData.add(data);
        }
        model.addAttribute("monthlyData", monthlyData);
        
        return "tutor/earnings";
    }

    private Integer getTutorIdFromAuth(Authentication auth) {
        try {
            System.out.println("Getting tutor ID from auth: " + auth.getName());
            Integer userId = getUserIdFromAuth(auth);
            System.out.println("Got user ID: " + userId);
            TutorDTO tutor = tutorService.getTutorByUserId(userId);
            System.out.println("Found tutor with ID: " + tutor.getId());
            return tutor.getId();
        } catch (Exception e) {
            System.err.println("Error getting tutor ID: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Integer getUserIdFromAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new RuntimeException("Invalid authentication type");
        }

        try {
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername(); // Email is used as username
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the user has tutor role
            if (!user.getRole().equals(Role.ROLE_TUTOR)) {
                throw new AccessDeniedException("User is not a tutor");
            }

            return user.getId();
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user information", e);
        }
    }

    @ExceptionHandler({RuntimeException.class, AccessDeniedException.class})
    public String handleError(Exception ex, Model model) {
        String errorMessage;
        String redirectUrl;

        if (ex instanceof AccessDeniedException) {
            errorMessage = "You don't have permission to access this page";
            redirectUrl = "/dashboard";
        } else if (ex.getMessage().contains("User not found")) {
            errorMessage = "Your account was not found. Please log in again";
            redirectUrl = "/auth/login";
        } else if (ex.getMessage().contains("User is not a tutor")) {
            errorMessage = "This page is only accessible to tutors";
            redirectUrl = "/dashboard";
        } else {
            errorMessage = "An error occurred: " + ex.getMessage();
            redirectUrl = "/dashboard";
        }

        model.addAttribute("error", errorMessage);
        model.addAttribute("redirectUrl", redirectUrl);
        return "error";
    }
}