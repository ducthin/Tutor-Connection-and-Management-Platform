package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.DayOfWeek;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.SubjectService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/tutors")
public class TutorSearchViewController {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TutorSearchViewController.class);

    @Autowired
    private TutorService tutorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    private void addSearchAttributes(Model model, String keyword, Integer subjectId,
            BigDecimal minRating, BigDecimal maxRate, String sortBy, List<TutorDTO> tutors) {
        model.addAttribute("tutors", tutors);
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("minRating", minRating);
        model.addAttribute("maxRate", maxRate);
        model.addAttribute("sortBy", sortBy);
    }

    @GetMapping("/search")
    public String searchPage(Model model) {
        List<SubjectDTO> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "tutor/search";
    }

    @GetMapping("/results")
    public String searchResults(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false, defaultValue = "RATING") String sortBy,
            Model model) {

        final String viewName = "tutor/search-results";
        List<TutorDTO> tutors = new ArrayList<>();
        model.addAttribute("subjects", subjectService.getAllSubjects());

        log.info("Search request received - subjectId: {}, keyword: {}, sortBy: {}",
                subjectId, keyword, sortBy);

        if (subjectId != null) {
            SubjectDTO subject;
            try {
                subject = subjectService.getSubjectById(subjectId);
                if (subject == null || !subject.isActive()) {
                    String errorMessage = subject == null ?
                            "The specified subject was not found" :
                            "The selected subject is not currently available";
                    log.warn("Subject validation failed: {}", errorMessage);
                    model.addAttribute("error", errorMessage);
                    addSearchAttributes(model, keyword, subjectId, minRating, maxRate, sortBy, tutors);
                    return viewName;
                }
                log.info("Using subject filter: {} ({})", subject.getName(), subjectId);
            } catch (Exception e) {
                log.error("Error validating subject: {}", e.getMessage(), e);
                model.addAttribute("error", "Unable to validate the selected subject. Please try again.");
                addSearchAttributes(model, keyword, subjectId, minRating, maxRate, sortBy, tutors);
                return viewName;
            }
        }

        try {
            tutors = tutorService.searchTutors(keyword, subjectId, minRating, maxRate, sortBy);
            log.info("Found {} tutors matching criteria", tutors.size());
            
            if (tutors.isEmpty()) {
                model.addAttribute("noResults", "No tutors found matching your criteria.");
            }
        } catch (Exception e) {
            log.error("Error during tutor search: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while processing your search. Please try with different criteria.");
            tutors = new ArrayList<>();
        }

        addSearchAttributes(model, keyword, subjectId, minRating, maxRate, sortBy, tutors);
        return viewName;
    }

    @GetMapping("/available")
    public String availableTutors(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String time,
            @RequestParam(required = false) Integer subjectId,
            Model model) {

        LocalTime searchTime = LocalTime.parse(time);
        List<TutorDTO> tutors = tutorService.findAvailableTutors(dayOfWeek, searchTime, subjectId);

        model.addAttribute("tutors", tutors);
        model.addAttribute("selectedDay", dayOfWeek);
        model.addAttribute("selectedTime", time);
        model.addAttribute("selectedSubject", subjectId);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        return "tutor/available-tutors";
    }

    @GetMapping("/favorites")
    public String favoriteTutors(Authentication auth, Model model) {
        Integer studentId = getStudentIdFromAuth(auth);
        List<TutorDTO> favorites = studentService.getFavoriteTutors(studentId);
        model.addAttribute("tutors", favorites);
        return "tutor/favorites";
    }

    @PostMapping("/favorites/{tutorId}")
    public String addFavorite(@PathVariable Integer tutorId, Authentication auth) {
        Integer studentId = getStudentIdFromAuth(auth);
        studentService.addFavoriteTutor(studentId, tutorId);
        return "redirect:/tutors/favorites";
    }

    @DeleteMapping("/favorites/{tutorId}")
    public String removeFavoriteDelete(@PathVariable Integer tutorId, Authentication auth) {
        Integer studentId = getStudentIdFromAuth(auth);
        studentService.removeFavoriteTutor(studentId, tutorId);
        return "redirect:/tutors/favorites";
    }
    
    @PostMapping("/favorites/{tutorId}/remove")
    public String removeFavorite(@PathVariable Integer tutorId, Authentication auth) {
        Integer studentId = getStudentIdFromAuth(auth);
        studentService.removeFavoriteTutor(studentId, tutorId);
        return "redirect:/tutors/favorites";
    }

    @GetMapping("/search-view-profile/{tutorId}")
    public String viewTutorProfile(@PathVariable Integer tutorId, Authentication auth, Model model) {
        try {
            log.info("Attempting to fetch tutor profile with ID: {}", tutorId);
            TutorDTO tutor = tutorService.getTutorById(tutorId);
            log.info("Successfully retrieved tutor: {}", tutor.getUser().getFullName());
            List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
            List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
            List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutorId, 5);
            Double averageRating = tutorService.getAverageRating(tutorId);

            // Check if this is the tutor viewing their own profile
            boolean isTutorProfile = false;
            if (auth != null) {
                UserDTO user = userService.getUserByEmail(auth.getName());
                TutorDTO currentTutor = tutorService.getTutorByUserId(user.getId());
                isTutorProfile = (currentTutor != null && currentTutor.getId().equals(tutorId));

                // If it's a student viewing, check if they can book
                if (!isTutorProfile) {
                    try {
                        StudentDTO student = studentService.getStudentByEmail(auth.getName());
                        boolean canBook = studentService.canBookSession(student.getId(), tutorId);
                        model.addAttribute("canBook", canBook);
                        if (!canBook) {
                            model.addAttribute("warning", "You need an active subscription to book sessions.");
                        }
                    } catch (Exception e) {
                        // Not a student, ignore
                    }
                }
            }

            // Check if tutor is in student's favorites
            boolean isInFavorites = false;
            if (auth != null && !isTutorProfile) {
                try {
                    Integer studentId = getStudentIdFromAuth(auth);
                    List<TutorDTO> favorites = studentService.getFavoriteTutors(studentId);
                    isInFavorites = favorites.stream()
                            .anyMatch(favTutor -> favTutor.getId().equals(tutorId));
                } catch (Exception e) {
                    // Not a student or other error, ignore
                }
            }

            model.addAttribute("tutor", tutor);
            model.addAttribute("tutorSubjects", tutorSubjects);
            model.addAttribute("availability", availability);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("isTutorProfile", isTutorProfile);
            model.addAttribute("isInFavorites", isInFavorites);

            return "profile/tutor";
        } catch (Exception e) {
            model.addAttribute("error", "Tutor not found or profile unavailable");
            model.addAttribute("isTutorProfile", false);
            model.addAttribute("isInFavorites", false);
            return "profile/tutor";
        }
    }

    /**
     * Chuyển hướng từ URL cũ (/profile) sang URL mới (/view-profile) 
     * để đảm bảo các liên kết cũ vẫn hoạt động
     */
    @GetMapping("/profile-redirect/{tutorId}")
    public String redirectFromOldProfile(@PathVariable Integer tutorId) {
        return "redirect:/tutors/profile/" + tutorId;
    }

    private Integer getStudentIdFromAuth(Authentication auth) {
        if (auth == null) {
            throw new RuntimeException("User not authenticated");
        }
        UserDTO userDTO = userService.getUserByEmail(auth.getName());
        return studentService.getStudentByUserId(userDTO.getId()).getId();
    }
}