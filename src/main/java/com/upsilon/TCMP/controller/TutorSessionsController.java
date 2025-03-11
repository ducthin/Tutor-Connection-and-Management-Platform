package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.SessionDTO;
import com.upsilon.TCMP.dto.TutorDTO;

import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for managing tutor's upcoming sessions
 */
@Controller
@RequestMapping("/tutor/sessions")
@PreAuthorize("hasRole('TUTOR')")
public class TutorSessionsController {

    private final SessionService sessionService;
    private final TutorService tutorService;

    @Autowired
    public TutorSessionsController(
            SessionService sessionService,
            TutorService tutorService,
            UserService userService) {
        this.sessionService = sessionService;
        this.tutorService = tutorService;
    }

    @GetMapping
    public String viewAllSessions(Model model, Authentication auth) {
        try {
            // Get tutor ID from authentication
            Integer tutorId = getTutorIdFromAuth(auth);
            
            // Get all upcoming sessions
            List<SessionDTO> upcomingSessions = sessionService.findUpcomingSessionsByTutorId(tutorId);
            List<SessionDTO> pendingSessions = sessionService.findPendingSessionsByTutorId(tutorId);
            List<SessionDTO> completedSessions = sessionService.findCompletedSessionsByTutorId(tutorId);
            
            // Add to model
            model.addAttribute("upcomingSessions", upcomingSessions);
            model.addAttribute("pendingSessions", pendingSessions);
            model.addAttribute("completedSessions", completedSessions);
            model.addAttribute("allSessions", upcomingSessions);
            
            return "tutor/upcoming-sessions";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading sessions: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/filter")
    public String filterSessions(@RequestParam("status") String status, Model model, Authentication auth) {
        try {
            Integer tutorId = getTutorIdFromAuth(auth);
            List<SessionDTO> sessions;
            
            // Filter sessions based on status
            switch (status) {
                case "PENDING":
                    sessions = sessionService.findPendingSessionsByTutorId(tutorId);
                    break;
                case "UPCOMING":
                    sessions = sessionService.findUpcomingSessionsByTutorId(tutorId);
                    break;
                case "COMPLETED":
                    sessions = sessionService.findCompletedSessionsByTutorId(tutorId);
                    break;
                case "ALL":
                default:
                    // Get all sessions
                    List<SessionDTO> allSessions = sessionService.getSessionsByTutorId(tutorId);
                    sessions = allSessions;
                    break;
            }
            
            model.addAttribute("filteredSessions", sessions);
            model.addAttribute("selectedStatus", status);
            
            return "tutor/upcoming-sessions :: sessionsList";
        } catch (Exception e) {
            model.addAttribute("error", "Error filtering sessions: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/{id}/confirm")
    public String confirmSession(@PathVariable("id") Integer sessionId, RedirectAttributes redirectAttributes) {
        try {
            sessionService.confirmSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "Buổi học đã được xác nhận thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xác nhận buổi học: " + e.getMessage());
        }
        return "redirect:/tutor/sessions";
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelSession(
            @PathVariable("id") Integer sessionId,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {
        try {
            sessionService.cancelSession(sessionId, reason);
            redirectAttributes.addFlashAttribute("success", "Buổi học đã được hủy thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy buổi học: " + e.getMessage());
        }
        return "redirect:/tutor/sessions";
    }
    
    @PostMapping("/{id}/complete")
    public String completeSession(@PathVariable("id") Integer sessionId, RedirectAttributes redirectAttributes) {
        try {
            sessionService.completeSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "Buổi học đã được đánh dấu là hoàn thành.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể hoàn thành buổi học: " + e.getMessage());
        }
        return "redirect:/tutor/sessions";
    }

    private Integer getTutorIdFromAuth(Authentication auth) {
        String email = auth.getName();
        TutorDTO tutor = tutorService.getTutorByEmail(email);
        if (tutor == null) {
            throw new RuntimeException("Could not retrieve tutor information");
        }
        return tutor.getId();
    }
}