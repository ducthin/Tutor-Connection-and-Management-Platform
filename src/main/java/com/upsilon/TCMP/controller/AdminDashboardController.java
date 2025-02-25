package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.*;
import com.upsilon.TCMP.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SystemMonitorService systemMonitorService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            // User statistics
            long totalUsers = userService.countAllUsers();
            long activeTutors = userService.countUsersByRoleAndStatus(Role.ROLE_TUTOR, true);
            long activeStudents = userService.countUsersByRoleAndStatus(Role.ROLE_STUDENT, true);

            // Session statistics
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            long todaySessions = sessionService.countSessionsBetween(startOfDay, endOfDay);

            // Recent activity
            List<ActivityDTO> recentActivity = systemMonitorService.getRecentActivity(10);

            // System status
            SystemStatusDTO systemStatus = systemMonitorService.getCurrentStatus();

            // Add data to model
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("activeTutors", activeTutors);
            model.addAttribute("activeStudents", activeStudents);
            model.addAttribute("todaySessions", todaySessions);
            model.addAttribute("recentActivity", recentActivity);
            model.addAttribute("diskUsage", systemStatus.getDiskUsagePercent());
            model.addAttribute("memoryUsage", systemStatus.getMemoryUsagePercent());

            // System service statuses
            model.addAttribute("databaseStatus", systemStatus.isDatabaseConnected());
            model.addAttribute("emailStatus", systemStatus.isEmailServiceWorking());
            model.addAttribute("paymentStatus", systemStatus.isPaymentGatewayWorking());
            model.addAttribute("storageStatus", systemStatus.isFileStorageWorking());

            return "dashboard/admin";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "dashboard/admin";
        }
    }

    @GetMapping("/activity")
    public String viewAllActivity(Model model) {
        try {
            List<ActivityDTO> allActivity = systemMonitorService.getAllActivity();
            model.addAttribute("activities", allActivity);
            return "admin/activity";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading activity log: " + e.getMessage());
            return "admin/activity";
        }
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        try {
            List<UserDTO> users = userService.getAllUsers();
            model.addAttribute("users", users);
            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "admin/users";
        }
    }

    @GetMapping("/tutors/verify")
    public String verifyTutors(Model model) {
        try {
            List<TutorDTO> pendingTutors = tutorService.getAllTutors().stream()
                .filter(tutor -> !tutor.getIsVerified())
                .collect(Collectors.toList());
            model.addAttribute("pendingTutors", pendingTutors);
            return "admin/verify-tutors";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading pending tutors: " + e.getMessage());
            return "admin/verify-tutors";
        }
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        try {
            // Add various system reports and statistics
            return "admin/reports";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reports: " + e.getMessage());
            return "admin/reports";
        }
    }

    @GetMapping("/settings")
    public String systemSettings(Model model) {
        try {
            // Add system settings
            return "admin/settings";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading settings: " + e.getMessage());
            return "admin/settings";
        }
    }

    @GetMapping("/logs")
    public String systemLogs(Model model) {
        try {
            List<SystemLogDTO> logs = systemMonitorService.getSystemLogs();
            model.addAttribute("logs", logs);
            return "admin/logs";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading system logs: " + e.getMessage());
            return "admin/logs";
        }
    }
}
