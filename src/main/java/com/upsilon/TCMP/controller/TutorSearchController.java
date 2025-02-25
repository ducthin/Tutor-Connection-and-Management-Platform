package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.TutorDTO;
import com.upsilon.TCMP.enums.DayOfWeek;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.SubjectService;
import com.upsilon.TCMP.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tutors")
public class TutorSearchController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TutorDTO>> searchTutors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer subjectId) {
        
        List<TutorDTO> tutors = tutorService.getAllTutors();
        
        if (subjectId != null) {
            List<TutorDTO> subjectTutors = subjectService.getTutorsBySubject(subjectId);
            tutors = tutors.stream()
                .filter(subjectTutors::contains)
                .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            final String searchTerm = keyword.toLowerCase();
            tutors = tutors.stream()
                .filter(tutor -> tutor.getUser().getFullName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        }

        return ResponseEntity.ok(tutors);
    }

    @GetMapping("/advanced-search")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TutorDTO>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false, defaultValue = "RATING") String sortBy) {
        
        List<TutorDTO> tutors = tutorService.getAllTutors();

        // Filter by subject
        if (subjectId != null) {
            List<TutorDTO> subjectTutors = subjectService.getTutorsBySubject(subjectId);
            tutors = tutors.stream()
                .filter(subjectTutors::contains)
                .collect(Collectors.toList());
        }

        // Filter by rating
        if (minRating != null) {
            tutors = tutors.stream()
                .filter(tutor -> BigDecimal.valueOf(tutorService.getAverageRating(tutor.getId()))
                    .compareTo(minRating) >= 0)
                .collect(Collectors.toList());
        }

        // Filter by name/keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            final String searchTerm = keyword.toLowerCase();
            tutors = tutors.stream()
                .filter(tutor -> tutor.getUser().getFullName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        }

        // Sort results
        if ("RATING".equals(sortBy)) {
            tutors = tutors.stream()
                .sorted((t1, t2) -> Double.compare(
                    tutorService.getAverageRating(t2.getId()),
                    tutorService.getAverageRating(t1.getId())))
                .collect(Collectors.toList());
        }

        return ResponseEntity.ok(tutors);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TutorDTO>> findAvailableTutors(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam(required = false) Integer subjectId) {
        
        // Use the tutorService's findAvailableTutors method which uses the repository query
        List<TutorDTO> availableTutors = tutorService.findAvailableTutors(dayOfWeek, time, subjectId);
        
        return ResponseEntity.ok(availableTutors);
    }

    @GetMapping("/favorites/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TutorDTO>> getFavoriteTutors(@PathVariable Integer studentId) {
        return ResponseEntity.ok(studentService.getFavoriteTutors(studentId));
    }

    @PostMapping("/favorites/{studentId}/{tutorId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> addFavoriteTutor(
            @PathVariable Integer studentId,
            @PathVariable Integer tutorId) {
        studentService.addFavoriteTutor(studentId, tutorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites/{studentId}/{tutorId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> removeFavoriteTutor(
            @PathVariable Integer studentId,
            @PathVariable Integer tutorId) {
        studentService.removeFavoriteTutor(studentId, tutorId);
        return ResponseEntity.ok().build();
    }
}