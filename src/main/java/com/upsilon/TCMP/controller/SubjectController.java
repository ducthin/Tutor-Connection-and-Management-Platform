package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.SubjectDTO;
import com.upsilon.TCMP.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/subjects")
public class SubjectController {
    
    private final SubjectService subjectService;
    
    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
    
    @GetMapping
    public String listSubjects(Model model) {
        try {
            List<SubjectDTO> subjects = subjectService.getAllSubjects();
            model.addAttribute("subjects", subjects);
            return "subjects/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading subjects: " + e.getMessage());
            return "error";
        }
    }
}