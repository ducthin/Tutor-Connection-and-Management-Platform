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

import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private SessionService sessionService;


    @GetMapping({"/", "/dashboard"})
    public String dashboard(Authentication auth, Model model) {
        try {
            Integer studentId = getStudentIdFromAuth(auth);
            StudentDTO student = studentService.getStudentById(studentId);

            // Get total sessions
            long totalSessions = sessionService.countSessionsByStudentId(studentId);

            // Get active subjects (subjects student is currently learning)
            int activeSubjects = studentService.countActiveSubjects(studentId);

            // Get favorite tutors
            List<TutorDTO> favoriteTutors = studentService.getFavoriteTutors(studentId);

            // Get upcoming sessions
            List<SessionDTO> upcomingSessions = sessionService.findUpcomingSessionsByStudentId(studentId);

            // Add all data to model
            model.addAttribute("student", student);
            model.addAttribute("totalSessions", totalSessions);
            model.addAttribute("activeSubjects", activeSubjects);
            model.addAttribute("favoriteTutors", favoriteTutors);
            model.addAttribute("upcomingSessions", upcomingSessions);

            return "dashboard/student";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "dashboard/student";
        }
    }

    private Integer getStudentIdFromAuth(Authentication auth) {
        // Get user email from authentication
        String email = auth.getName();
        // Get student by email
        return studentService.getStudentByEmail(email).getId();
    }
}