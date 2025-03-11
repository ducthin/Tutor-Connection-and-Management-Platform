package com.upsilon.TCMP.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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