package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.DayOfWeek;
import com.upsilon.TCMP.repository.TutorSubjectRepository;
import com.upsilon.TCMP.service.SubjectService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/tutor")
@PreAuthorize("hasRole('TUTOR')")
public class TutorController  {
    @Autowired
private TutorService tutorService;

@Autowired
private UserService userService;

@Autowired
private SubjectService subjectService;

@Autowired
private TutorSubjectRepository tutorSubjectRepository;

@GetMapping("/profile")
public String viewProfile(Authentication auth, Model model) {
    try {
        UserDTO user = userService.getUserByEmail(auth.getName());
        TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
        
        List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutor.getId());
        List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutor.getId());
        List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutor.getId(), 5);
        Double averageRating = tutorService.getAverageRating(tutor.getId());
        
        model.addAttribute("tutor", tutor);
        model.addAttribute("user", user);
        model.addAttribute("tutorSubjects", tutorSubjects);
        model.addAttribute("availability", availability);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("averageRating", averageRating);
        
        return "profile/tutor";
    } catch (Exception e) {
        model.addAttribute("error", "Failed to load profile: " + e.getMessage());
        return "redirect:/dashboard";
    }
}

@GetMapping("/subjects")
public String subjects(Authentication auth, Model model) {
    try {
        UserDTO user = userService.getUserByEmail(auth.getName());
        TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
        
        List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutor.getId());
        List<SubjectDTO> allSubjects = subjectService.getAllSubjects();
        
        model.addAttribute("tutorSubjects", tutorSubjects);
        model.addAttribute("allSubjects", allSubjects);
        
        return "tutor/subjects";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return "redirect:/dashboard";
    }
}

@GetMapping("/availability")
public String availability(Authentication auth, Model model) {
    try {
        UserDTO user = userService.getUserByEmail(auth.getName());
        TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
        
        List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutor.getId());
        
        // Generate time slots (8 AM to 8 PM)
        List<String> timeSlots = new ArrayList<>();
        LocalTime time = LocalTime.of(8, 0);
        while (!time.isAfter(LocalTime.of(20, 0))) {
            timeSlots.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            time = time.plusHours(1);
        }
        
        // Create availability matrix
        Map<DayOfWeek, Map<String, TutorAvailabilityDTO>> availabilityMatrix = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            availabilityMatrix.put(day, new HashMap<>());
        }
        
        for (TutorAvailabilityDTO slot : availability) {
            availabilityMatrix.get(slot.getDayOfWeek())
                .put(slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")), slot);
        }
        
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("days", DayOfWeek.values());
        model.addAttribute("availabilityMatrix", availabilityMatrix);
        
        return "tutor/availability";
    } catch (Exception e) {
        model.addAttribute("error", "Error loading availability: " + e.getMessage());
        return "tutor/availability";
    }
}


    @PostMapping("/subjects/add")
    public String addSubject(
            Authentication auth,
            @Valid @ModelAttribute TutorSubjectCreateDTO createDTO,
            BindingResult bindingResult,
            Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("error", "Invalid input. Please check your data.");
                return subjects(auth, model);
            }

            UserDTO user = userService.getUserByEmail(auth.getName());
            TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
            createDTO.setTutorId(tutor.getId());
            
            tutorService.addSubject(tutor.getId(), createDTO);
            model.addAttribute("success", "Subject added successfully");
            return "redirect:/tutor/subjects";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return subjects(auth, model);
        }
    }

    @PostMapping("/subjects/{id}/delete")
    public String deleteSubject(Authentication auth, @PathVariable("id") Integer subjectId, Model model) {
        try {
            UserDTO user = userService.getUserByEmail(auth.getName());
            TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
            tutorService.removeSubject(tutor.getId(), subjectId);
            return "redirect:/tutor/subjects";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tutor/subjects";
        }
    }

   @PostMapping("/availability/add")
   public String addAvailability(
           Authentication auth,
           @RequestParam DayOfWeek dayOfWeek,
           @RequestParam String startTime,
           @RequestParam String endTime,
           @RequestParam(defaultValue = "false") boolean isRecurring,
           Model model) {
       try {
           UserDTO user = userService.getUserByEmail(auth.getName());
           TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
           
           TutorAvailabilityCreateDTO createDTO = new TutorAvailabilityCreateDTO();
           createDTO.setTutorId(tutor.getId());
           createDTO.setDayOfWeek(dayOfWeek);
           createDTO.setStartTime(LocalTime.parse(startTime));
           createDTO.setEndTime(LocalTime.parse(endTime));
           createDTO.setIsRecurring(isRecurring);
           
           tutorService.addAvailability(createDTO);
           return "redirect:/tutor/availability";
       } catch (Exception e) {
           model.addAttribute("error", e.getMessage());
           return "redirect:/tutor/availability";
       }
   }

   @GetMapping("/sessions")
   public String sessions(Authentication auth, Model model) {
       try {
           UserDTO user = userService.getUserByEmail(auth.getName());
           TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
           List<SessionDTO> sessions = tutorService.getTutorSessions(tutor.getId());
           
           model.addAttribute("tutor", tutor);
           model.addAttribute("sessions", sessions);
           return "tutor/sessions";
       } catch (Exception e) {
           model.addAttribute("error", e.getMessage());
           return "redirect:/dashboard";
       }
   }

   @GetMapping("/reviews")
   public String reviews(Authentication auth, Model model) {
       try {
           UserDTO user = userService.getUserByEmail(auth.getName());
           TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
           
           List<ReviewDTO> reviews = tutorService.getTutorReviews(tutor.getId());
           Double averageRating = tutorService.getAverageRating(tutor.getId());
           
           model.addAttribute("tutor", tutor);
           model.addAttribute("reviews", reviews);
           model.addAttribute("averageRating", averageRating);
           return "tutor/reviews";
       } catch (Exception e) {
           model.addAttribute("error", e.getMessage());
           return "redirect:/dashboard";
       }
   }

    @PostMapping("/availability/{id}/delete")
    public String deleteAvailability(
            Authentication auth,
            @PathVariable("id") Integer availabilityId,
            Model model) {
        try {
            UserDTO user = userService.getUserByEmail(auth.getName());
            TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
            tutorService.removeAvailability(availabilityId);
            return "redirect:/tutor/availability";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tutor/availability";
        }
    }
}
