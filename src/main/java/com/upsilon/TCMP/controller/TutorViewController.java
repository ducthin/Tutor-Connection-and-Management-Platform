package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.ReviewService;
import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import com.upsilon.TCMP.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller chuyên dụng để xử lý việc xem profile của tutor
 * Giải quyết vấn đề xung đột giữa các controller khác nhau
 */
@Controller
@RequestMapping("/tutors/view-profile")
public class TutorViewController {
    
    private static final Logger log = LoggerFactory.getLogger(TutorViewController.class);
    
    @Autowired
    private TutorService tutorService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;
    
    /**
     * Phương thức xử lý URL /tutors/view-profile/{id}
     * Chuyển hướng đến controller chính xử lý việc xem profile
     */
    @GetMapping("/{id}")
    public String viewTutorProfile(@PathVariable("id") Integer tutorId) {
        log.info("Redirecting from /tutors/view-profile/{} to /tutors/profile/{}", tutorId, tutorId);
        return "redirect:/tutors/profile/" + tutorId;
    }
} 