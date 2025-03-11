package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.Role;
import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.StudentService;
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
 * Manages session operations like confirmation, cancellation, completion
 * Note: For session booking, use SessionBookingController instead
 */
@Controller
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final StudentService studentService;
    private final UserService userService;
    private final TutorService tutorService;

    @Autowired
    public SessionController(
            SessionService sessionService, 
            StudentService studentService,
            UserService userService,
            TutorService tutorService) {
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.userService = userService;
        this.tutorService = tutorService;
    }

    @GetMapping
    public String viewAllSessions(Model model, Authentication auth) {
        UserDTO userDTO = userService.getUserByEmail(auth.getName());
        if (userDTO.getRole() == Role.ROLE_TUTOR) {
            return "redirect:/sessions/tutor";
        } else if (userDTO.getRole() == Role.ROLE_STUDENT) {
            return "redirect:/sessions/student";
        } else {
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public String viewTutorSessions(Model model, Authentication auth) {
        UserDTO userDTO = userService.getUserByEmail(auth.getName());
        TutorDTO tutorDTO = tutorService.getTutorByUserId(userDTO.getId());
        
        List<SessionDTO> upcomingSessions = sessionService.findUpcomingSessionsByTutorId(tutorDTO.getId());
        List<SessionDTO> completedSessions = sessionService.findCompletedSessionsByTutorId(tutorDTO.getId());
        List<SessionDTO> pendingSessions = sessionService.findPendingSessionsByTutorId(tutorDTO.getId());
        
        model.addAttribute("upcomingSessions", upcomingSessions);
        model.addAttribute("completedSessions", completedSessions);
        model.addAttribute("pendingSessions", pendingSessions);
        model.addAttribute("sessions", upcomingSessions);
        
        return "tutor/sessions";
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public String viewStudentSessions(Model model, Authentication auth) {
        UserDTO userDTO = userService.getUserByEmail(auth.getName());
        StudentDTO studentDTO = studentService.getStudentByUserId(userDTO.getId());
        
        List<SessionDTO> upcomingSessions = sessionService.findUpcomingSessionsByStudentId(studentDTO.getId());
        List<SessionDTO> completedSessions = sessionService.findCompletedSessionsByStudentId(studentDTO.getId());
        
        model.addAttribute("upcomingSessions", upcomingSessions);
        model.addAttribute("completedSessions", completedSessions);
        
        return "student/sessions";
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('TUTOR')")
    public String confirmSession(@PathVariable("id") Integer sessionId, RedirectAttributes redirectAttributes) {
        try {
            sessionService.confirmSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "Buổi học đã được xác nhận thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xác nhận buổi học: " + e.getMessage());
        }
        return "redirect:/sessions/tutor";
    }

    @PostMapping("/{id}/cancel")
    public String cancelSession(
            @PathVariable("id") Integer sessionId, 
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        try {
            UserDTO userDTO = userService.getUserByEmail(auth.getName());
            // Xác thực xem người dùng có quyền hủy buổi học hay không
            // Commented out unused variable: sessionService.getSessionById(sessionId);
            
            // Thực hiện hủy buổi học
            sessionService.cancelSession(sessionId, reason);
            redirectAttributes.addFlashAttribute("success", "Buổi học đã được hủy.");
            
            // Chuyển hướng dựa vào vai trò người dùng
            if (userDTO.getRole() == Role.ROLE_TUTOR) {
                return "redirect:/sessions/tutor";
            } else {
                return "redirect:/sessions/student";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy buổi học: " + e.getMessage());
            return "redirect:/sessions";
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('TUTOR')")
    public String completeSession(@PathVariable("id") Integer sessionId, RedirectAttributes redirectAttributes) {
        try {
            sessionService.completeSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "Buổi học đã được đánh dấu là hoàn thành.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể hoàn thành buổi học: " + e.getMessage());
        }
        return "redirect:/sessions/tutor";
    }

    @GetMapping("/{id}")
    public String viewSessionDetails(@PathVariable("id") Integer sessionId, Model model, Authentication auth) {
        try {
            SessionDTO sessionDTO = sessionService.getSessionById(sessionId);
            model.addAttribute("session", sessionDTO);
            return "session/details";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tìm thấy buổi học: " + e.getMessage());
            return "error";
        }
    }
}