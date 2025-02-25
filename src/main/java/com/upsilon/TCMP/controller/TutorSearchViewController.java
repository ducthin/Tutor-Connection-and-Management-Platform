package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.SubjectDTO;
import com.upsilon.TCMP.dto.TutorDTO;
import com.upsilon.TCMP.dto.UserDTO;
import com.upsilon.TCMP.enums.DayOfWeek;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.SubjectService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/tutors")
public class TutorSearchViewController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String index(Model model) {
        List<TutorDTO> featuredTutors = tutorService.getAllTutors().stream()
            .filter(tutor -> {
                Double rating = tutorService.getAverageRating(tutor.getId());
                return rating != null && BigDecimal.valueOf(rating)
                    .compareTo(new BigDecimal("4.5")) >= 0;
            })
            .toList();
        model.addAttribute("featuredTutors", featuredTutors);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        return "tutor/index";
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('STUDENT')")
    public String searchPage(Model model) {
        List<SubjectDTO> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "tutor/search";
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('STUDENT')")
    public String searchResults(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false, defaultValue = "RATING") String sortBy,
            Model model) {

        // Use the tutor service's optimized search method
        List<TutorDTO> tutors = tutorService.searchTutors(keyword, subjectId, minRating, maxRate, sortBy);

        model.addAttribute("tutors", tutors);
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("minRating", minRating);
        model.addAttribute("maxRate", maxRate);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        return "tutor/search-results";
    }

    @GetMapping("/available")
    public String availableTutors(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String time,
            @RequestParam(required = false) Integer subjectId,
            Model model) {

        LocalTime searchTime = LocalTime.parse(time);
        
        // Use the tutor service to find available tutors
        List<TutorDTO> tutors = tutorService.findAvailableTutors(dayOfWeek, searchTime, subjectId);

        model.addAttribute("tutors", tutors);
        model.addAttribute("selectedDay", dayOfWeek);
        model.addAttribute("selectedTime", time);
        model.addAttribute("selectedSubject", subjectId);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        return "tutor/available-tutors";
    }

    @GetMapping("/favorites")
    public String favoriteTutors(Authentication auth, Model model) {
        Integer studentId = getStudentIdFromAuth(auth);
        List<TutorDTO> favorites = studentService.getFavoriteTutors(studentId);
        model.addAttribute("tutors", favorites);
        return "tutor/favorites";
    }

    @PostMapping("/favorites/{tutorId}")
    public String addFavorite(@PathVariable Integer tutorId, Authentication auth) {
        Integer studentId = getStudentIdFromAuth(auth);
        studentService.addFavoriteTutor(studentId, tutorId);
        return "redirect:/tutors/favorites";
    }

    @DeleteMapping("/favorites/{tutorId}")
    public String removeFavorite(@PathVariable Integer tutorId, Authentication auth) {
        Integer studentId = getStudentIdFromAuth(auth);
        studentService.removeFavoriteTutor(studentId, tutorId);
        return "redirect:/tutors/favorites";
    }

    private Integer getStudentIdFromAuth(Authentication auth) {
        if (auth == null) {
            throw new RuntimeException("User not authenticated");
        }
        UserDTO userDTO = userService.getUserByEmail(auth.getName());
        return studentService.getStudentByUserId(userDTO.getId()).getId();
    }
}