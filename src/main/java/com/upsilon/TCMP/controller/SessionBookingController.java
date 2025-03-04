package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/book")
@PreAuthorize("hasRole('STUDENT')")
public class SessionBookingController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @GetMapping("/tutor/{id}")
    public String showBookingForm(@PathVariable("id") Integer tutorId, Model model) {
        // Get tutor information
        TutorDTO tutor = tutorService.getTutorById(tutorId);
        if (tutor == null) {
            model.addAttribute("error", "Tutor not found");
            return "tutor/book-session";
        }

        // Get tutor's subjects
        List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
        if (tutorSubjects.isEmpty()) {
            model.addAttribute("error", "This tutor has no available subjects. Please ask them to add subjects before booking.");
            model.addAttribute("tutor", tutor);
            return "tutor/book-session";
        }

        // Get tutor's availability
        List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
        if (availability.isEmpty()) {
            model.addAttribute("error", "This tutor has not set their availability yet. Please ask them to set their schedule before booking.");
            model.addAttribute("tutor", tutor);
            model.addAttribute("tutorSubjects", tutorSubjects);
            return "tutor/book-session";
        }

        // Add all necessary data to the model
        model.addAttribute("tutor", tutor);
        model.addAttribute("tutorSubjects", tutorSubjects);
        model.addAttribute("availability", availability);

        return "tutor/book-session";
    }

    @PostMapping("/session")
    public String bookSession(
            @RequestParam("tutorId") Integer tutorId,
            @RequestParam("subjectId") Integer subjectId,
            @RequestParam("sessionDate") String sessionDate,
            @RequestParam("timeSelection") String timeSelection,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication auth,
            Model model) {
        try {
            // Get current student
            UserDTO user = userService.getUserByEmail(auth.getName());
            StudentDTO student = studentService.getStudentByUserId(user.getId());

            // Parse time selection
            String[] times = timeSelection.split(",");
            if (times.length != 2) {
                throw new IllegalArgumentException("Invalid time selection");
            }

            // Create DTO
            SessionCreateDTO createDTO = new SessionCreateDTO();
            createDTO.setStudentId(student.getId());
            createDTO.setTutorId(tutorId);
            createDTO.setSubjectId(subjectId);
            createDTO.setSessionDate(sessionDate);
            createDTO.setStartTime(times[0]);
            createDTO.setEndTime(times[1]);
            createDTO.setNotes(notes);

            // Create the session
            SessionDTO sessionDTO = sessionService.createSession(createDTO);
            
            model.addAttribute("success", "Session booked successfully! Please wait for the tutor's confirmation.");
            return "redirect:/tutors/profile/" + tutorId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/book/tutor/" + tutorId;
        }
    }
}