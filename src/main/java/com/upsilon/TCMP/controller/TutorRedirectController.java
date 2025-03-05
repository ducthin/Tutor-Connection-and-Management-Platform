package com.upsilon.TCMP.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tutors/redirect-profile")
public class TutorRedirectController {
    
    private static final Logger log = LoggerFactory.getLogger(TutorRedirectController.class);
    
    @GetMapping("/{id}")
    public String redirectToProfile(@PathVariable("id") Integer tutorId) {
        log.info("Redirecting from /tutors/redirect-profile/{} to /tutors/profile/{}", tutorId, tutorId);
        return "redirect:/tutors/profile/" + tutorId;
    }
} 