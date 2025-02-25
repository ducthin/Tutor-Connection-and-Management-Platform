package com.upsilon.TCMP.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.upsilon.TCMP.config.CustomUserDetailsService;
import com.upsilon.TCMP.dto.TutorDTO;
import com.upsilon.TCMP.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public DashboardController(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        String userEmail = authentication.getName();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
        model.addAttribute("user", userDetails);

        try {
            if (roles.contains("ROLE_ADMIN")) {
                return "redirect:/dashboard/admin";
            } else if (roles.contains("ROLE_TUTOR")) {
                return "redirect:/dashboard/tutor";
            } else if (roles.contains("ROLE_STUDENT")) {
                return "redirect:/dashboard/student";
            }
            
            return "dashboard/index";
        } catch (Exception e) {
            // Log the error
            logger.error("Error in dashboard routing: ", e);
            model.addAttribute("error", "Error loading dashboard. Please try again.");
            return "dashboard/index";
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
            model.addAttribute("user", userDetails);

            // Initialize statistics with 0 to avoid null pointer exceptions
            model.addAttribute("totalStudents", 0);
            model.addAttribute("totalTutors", 0);
            model.addAttribute("totalSessions", 0);
            model.addAttribute("totalSubjects", 0);
            
            return "dashboard/admin";
        } catch (Exception e) {
            logger.error("Error loading admin dashboard: ", e);
            model.addAttribute("error", "Error loading admin dashboard. Please try again.");
            return "dashboard/index";
        }
    }

    @Autowired
    private TutorService tutorService;

    @GetMapping("/tutor")
    public String tutorDashboard() {
        return "redirect:/tutor/dashboard";
    }

    @GetMapping("/student")
    public String studentDashboard() {
        return "redirect:/student/dashboard";
    }
}