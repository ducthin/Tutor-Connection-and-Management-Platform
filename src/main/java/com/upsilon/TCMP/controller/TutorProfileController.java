package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tutor-profile")
@PreAuthorize("hasRole('TUTOR')")
@CrossOrigin
public class TutorProfileController {

    @Autowired
    private TutorService tutorService;

    @GetMapping("/{tutorId}")
    public ResponseEntity<TutorDTO> getTutorProfile(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTutorById(tutorId));
    }

    @PostMapping(value = "/{tutorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TutorDTO> updateProfile(
            @PathVariable Integer tutorId,
            @ModelAttribute TutorProfileUpdateDTO updateDTO) {
        return ResponseEntity.ok(tutorService.updateTutorProfile(tutorId, updateDTO));
    }

    @PostMapping("/{tutorId}/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Integer tutorId,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = tutorService.uploadProfilePicture(tutorId, file);
        return ResponseEntity.ok(imageUrl);
    }

    // Tutor Subject Management
    @GetMapping("/{tutorId}/subjects")
    public ResponseEntity<List<TutorSubjectDTO>> getTutorSubjects(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTutorSubjects(tutorId));
    }

    @PostMapping("/{tutorId}/subjects")
    public ResponseEntity<TutorSubjectDTO> addSubject(
            @PathVariable Integer tutorId,
            @RequestBody TutorSubjectCreateDTO createDTO) {
        createDTO.setTutorId(tutorId);
        return ResponseEntity.ok(tutorService.addSubject(tutorId, createDTO));
    }

    @PutMapping("/{tutorId}/subjects/{subjectId}")
    public ResponseEntity<TutorSubjectDTO> updateSubject(
            @PathVariable Integer tutorId,
            @PathVariable Integer subjectId,
            @RequestBody TutorSubjectUpdateDTO updateDTO) {
        return ResponseEntity.ok(tutorService.updateSubject(tutorId, subjectId, updateDTO));
    }

    @DeleteMapping("/{tutorId}/subjects/{subjectId}")
    public ResponseEntity<Void> removeSubject(
            @PathVariable Integer tutorId,
            @PathVariable Integer subjectId) {
        tutorService.removeSubject(tutorId, subjectId);
        return ResponseEntity.ok().build();
    }

    // Tutor Availability Management
    @GetMapping("/{tutorId}/availability")
    public ResponseEntity<List<TutorAvailabilityDTO>> getAvailability(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTutorAvailability(tutorId));
    }

    @PostMapping("/{tutorId}/availability")
    public ResponseEntity<TutorAvailabilityDTO> addAvailability(
            @PathVariable Integer tutorId,
            @RequestBody TutorAvailabilityCreateDTO createDTO) {
        createDTO.setTutorId(tutorId);
        return ResponseEntity.ok(tutorService.addAvailability(createDTO));
    }

    @PostMapping("/{tutorId}/availability/bulk")
    public ResponseEntity<Void> addBulkAvailability(
            @PathVariable Integer tutorId,
            @RequestBody BulkAvailabilityCreateDTO createDTO) {
        createDTO.setTutorId(tutorId);
        tutorService.addBulkAvailability(createDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tutorId}/availability/{availabilityId}")
    public ResponseEntity<Void> removeAvailability(
            @PathVariable Integer tutorId,
            @PathVariable Integer availabilityId) {
        tutorService.removeAvailability(availabilityId);
        return ResponseEntity.ok().build();
    }

    // Sessions and Earnings
    @GetMapping("/{tutorId}/sessions")
    public ResponseEntity<List<SessionDTO>> getSessions(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTutorSessions(tutorId));
    }

    @GetMapping("/{tutorId}/upcoming-sessions")
    public ResponseEntity<List<SessionDTO>> getUpcomingSessions(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTutorUpcomingSessions(tutorId));
    }

    @GetMapping("/{tutorId}/session-count")
    public ResponseEntity<Long> getTotalSessionCount(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTotalSessionCount(tutorId));
    }

    @GetMapping("/{tutorId}/earnings")
    public ResponseEntity<Double> getTotalEarnings(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.calculateTotalEarnings(tutorId));
    }

    @GetMapping("/{tutorId}/earnings/{year}/{month}")
    public ResponseEntity<Double> getMonthlyEarnings(
            @PathVariable Integer tutorId,
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return ResponseEntity.ok(tutorService.calculateMonthlyEarnings(tutorId, year, month));
    }

    // Reviews
    @GetMapping("/{tutorId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getTutorReviews(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getTutorReviews(tutorId));
    }

    @GetMapping("/{tutorId}/reviews/recent")
    public ResponseEntity<List<ReviewDTO>> getRecentReviews(
            @PathVariable Integer tutorId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(tutorService.getRecentTutorReviews(tutorId, limit));
    }

    @GetMapping("/{tutorId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Integer tutorId) {
        return ResponseEntity.ok(tutorService.getAverageRating(tutorId));
    }
}