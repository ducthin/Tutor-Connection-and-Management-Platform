package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tutor")
@PreAuthorize("hasRole('TUTOR')")
public class TutorDashboardController {

    @Autowired
    private TutorService tutorService;


    @GetMapping({"/", "/dashboard"})
    public String dashboard(Authentication auth, Model model) {
        try {
            System.out.println("Loading tutor dashboard for user: " + auth.getName());
            
            // Get tutor ID from authentication
            Integer tutorId = getTutorIdFromAuth(auth);
            System.out.println("Found tutor ID: " + tutorId);

            // Get tutor details
            TutorDTO tutor = tutorService.getTutorById(tutorId);
            System.out.println("Loaded tutor: " + (tutor != null ? "ID=" + tutor.getId() : "null"));
            if (tutor == null) {
                throw new RuntimeException("Failed to load tutor data");
            }

            // Get tutor subjects
            List<TutorSubjectDTO> subjects = tutorService.getTutorSubjects(tutorId);
            model.addAttribute("subjects", subjects);

            // Get upcoming sessions
            List<SessionDTO> upcomingSessions = tutorService.getTutorUpcomingSessions(tutorId);
            System.out.println("Loaded " + upcomingSessions.size() + " upcoming sessions");

            // Get recent reviews
            List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutorId, 5);
            System.out.println("Loaded " + recentReviews.size() + " recent reviews");

            // Calculate monthly earnings
            double monthlyEarnings = tutorService.calculateMonthlyEarnings(
                tutorId,
                LocalDateTime.now().getYear(),
                LocalDateTime.now().getMonthValue()
            );
            System.out.println("Calculated monthly earnings: " + monthlyEarnings);

            // Get total session count
            long totalSessions = tutorService.getTotalSessionCount(tutorId);
            System.out.println("Total sessions: " + totalSessions);

            // Get average rating
            Double averageRating = tutorService.getAverageRating(tutorId);
            double rating = averageRating != null ? averageRating : 0.0;
            System.out.println("Average rating: " + rating);

            // Add all attributes to model
            model.addAttribute("tutor", tutor);
            model.addAttribute("upcomingSessions", upcomingSessions);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("monthlyEarnings", monthlyEarnings);
            model.addAttribute("totalSessions", totalSessions);
            model.addAttribute("averageRating", rating);
            
            // Add quick stats
            int activeSubjects = tutorService.getActiveSubjectCount(tutorId);
            double completionRate = tutorService.getSessionCompletionRate(tutorId);
            int totalStudents = tutorService.getUniqueStudentCount(tutorId);
            
            System.out.println("Quick stats - Active subjects: " + activeSubjects +
                             ", Completion rate: " + completionRate +
                             ", Total students: " + totalStudents);
            
            model.addAttribute("activeSubjects", activeSubjects);
            model.addAttribute("completionRate", completionRate);
            model.addAttribute("totalStudents", totalStudents);

            System.out.println("Successfully loaded all dashboard data");
            return "dashboard/tutor";
        } catch (Exception e) {
            System.err.println("Error loading tutor dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    private Integer getTutorIdFromAuth(Authentication auth) {
        try {
            String email = auth.getName();
            System.out.println("Getting tutor ID for authenticated user: " + email);
            
            TutorDTO tutor = tutorService.getTutorByEmail(email);
            if (tutor == null) {
                System.err.println("getTutorByEmail returned null for email: " + email);
                throw new RuntimeException("Could not retrieve tutor information");
            }
            
            System.out.println("Found tutor ID: " + tutor.getId() + " for email: " + email);
            return tutor.getId();
        } catch (Exception e) {
            System.err.println("Error in getTutorIdFromAuth: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}