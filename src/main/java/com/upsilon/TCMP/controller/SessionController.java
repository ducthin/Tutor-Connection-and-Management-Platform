package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.SessionStatus;
import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final StudentService studentService;

    @Autowired
    public SessionController(SessionService sessionService, StudentService studentService) {
        this.sessionService = sessionService;
        this.studentService = studentService;
    }

    @PostMapping("/book")
    public String bookSession(SessionCreateDTO createDTO, 
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            // Get current student from logged-in user
            StudentDTO studentDTO = studentService.getStudentByEmail(userDetails.getUsername());
            createDTO.setStudentId(studentDTO.getId());

            // Convert date and time strings to LocalDateTime
            LocalDate date = LocalDate.parse(createDTO.getSessionDate());
            LocalTime startTime = LocalTime.parse(createDTO.getStartTime());
            LocalTime endTime = LocalTime.parse(createDTO.getEndTime());

            SessionDTO sessionDTO = new SessionDTO();
            sessionDTO.setStartTime(LocalDateTime.of(date, startTime));
            sessionDTO.setEndTime(LocalDateTime.of(date, endTime));
            sessionDTO.setNotes(createDTO.getNotes());
            sessionDTO.setStatus(SessionStatus.PENDING);

            // Set up relationships
            StudentDTO student = new StudentDTO();
            student.setId(studentDTO.getId());
            sessionDTO.setStudent(student);

            // Set up tutor reference
            TutorDTO tutor = new TutorDTO();
            tutor.setId(createDTO.getTutorId());
            sessionDTO.setTutor(tutor);

            // Set up subject reference
            SubjectDTO subject = new SubjectDTO();
            subject.setId(createDTO.getSubjectId());
            sessionDTO.setSubject(subject);

            sessionService.createSession(sessionDTO);

            redirectAttributes.addFlashAttribute("success", "Session booked successfully!");
            return "redirect:/tutors/profile/" + createDTO.getTutorId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to book session: " + e.getMessage());
            return "redirect:/tutors/profile/" + createDTO.getTutorId();
        }
    }
}