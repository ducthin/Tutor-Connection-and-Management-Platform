package com.upsilon.TCMP.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.upsilon.TCMP.dto.StudentDTO;
import com.upsilon.TCMP.dto.UserDTO;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.UserService;

@Controller
@RequestMapping("/student")
public class StudentProfileController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        try {
            UserDTO user = userService.getUserByEmail(authentication.getName());
            // Get student with sessions loaded
            StudentDTO student = studentService.getStudentWithSessions(user.getId());

            model.addAttribute("user", user);
            model.addAttribute("student", student);

            // Add debug info
            model.addAttribute("hasStudent", student != null);
            if (student != null) {
                model.addAttribute("hasSessions", student.getSessions() != null);
                model.addAttribute("sessionCount", student.getSessions() != null ? student.getSessions().size() : 0);
            }

            return "profile/student";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
            return "error";
        }
    }
}