package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.DayOfWeek;
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
import java.util.ArrayList;

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

@GetMapping("/subjects")
public String subjects(Authentication auth, Model model) {
    try {
        UserDTO user = userService.getUserByEmail(auth.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
        if (tutor == null) {
            throw new RuntimeException("Tutor not found");
        }
        
        List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutor.getId());
        List<SubjectDTO> allSubjects = subjectService.getAllSubjects();
        
        model.addAttribute("tutorSubjects", tutorSubjects);
        model.addAttribute("allSubjects", allSubjects);
        
        return "tutor/subjects";
    } catch (Exception e) {
        model.addAttribute("error", "Failed to load subjects: " + e.getMessage());
        return "tutor/subjects"; // Stay on subjects page to show error
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


    @PostMapping("/add-subject")
    public String addSubject(
            Authentication auth,
            @Valid @ModelAttribute TutorSubjectCreateDTO createDTO,
            BindingResult bindingResult,
            Model model) {
        try {
            System.out.println("=== Processing POST /tutor/add-subject ===");
            System.out.println("Subject ID: " + createDTO.getSubjectId());
            System.out.println("Experience Years: " + createDTO.getExperienceYears());
            System.out.println("Rate: " + createDTO.getRate());
            
            if (bindingResult.hasErrors()) {
                System.out.println("Validation errors found:");
                bindingResult.getAllErrors().forEach(error ->
                    System.out.println(" - " + error.getDefaultMessage())
                );
                model.addAttribute("error", "Invalid input. Please check your data.");
                return showAddNewSubjectForm(auth, model);
            }

            UserDTO user = userService.getUserByEmail(auth.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
            if (tutor == null) {
                throw new RuntimeException("Tutor not found");
            }
            
            createDTO.setTutorId(tutor.getId());
            
            System.out.println("Adding subject for tutor ID: " + tutor.getId());
            tutorService.addSubject(tutor.getId(), createDTO);
            
            System.out.println("Subject added successfully");
            System.out.println("=====================================");
            
            model.addAttribute("success", "Subject added successfully");
            return "redirect:/tutor/subjects";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return subjects(auth, model);
        }
    }
@GetMapping("/add-subject")
public String showAddNewSubjectForm(Authentication auth, Model model) {
    try {
        // Get the tutor information
        UserDTO user = userService.getUserByEmail(auth.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
        if (tutor == null) {
            throw new RuntimeException("Tutor not found");
        }

        // Get list of subjects
        List<SubjectDTO> allSubjects = subjectService.getAllSubjects();
        if (allSubjects == null || allSubjects.isEmpty()) {
            System.out.println("Warning: No subjects found in the database");
            allSubjects = new ArrayList<>(); // Ensure we at least have an empty list
        }

        System.out.println("=== Debug Info for /tutor/add-subject ===");
        System.out.println("User email: " + auth.getName());
        System.out.println("Tutor ID: " + tutor.getId());
        System.out.println("Number of subjects found: " + allSubjects.size());
        allSubjects.forEach(subject ->
            System.out.println(" - " + subject.getName() + " (ID: " + subject.getId() + ", Active: " + subject.isActive() + ")")
        );
        System.out.println("=====================================");

        // Add required attributes to model
        model.addAttribute("tutor", tutor);
        model.addAttribute("allSubjects", allSubjects);
        model.addAttribute("user", user);
        return "tutor/add-subject";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tutor/subjects";
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

   @GetMapping("/sessions-overview")
   public String sessions(Authentication auth, Model model) {
       try {
           UserDTO user = userService.getUserByEmail(auth.getName());
           TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
           
           // Get all sessions with proper categorization
           List<SessionDTO> allSessions = tutorService.getTutorSessions(tutor.getId());
           List<SessionDTO> upcomingSessions = tutorService.getTutorUpcomingSessions(tutor.getId());
           List<SessionDTO> pendingSessions = new ArrayList<>();
           List<SessionDTO> completedSessions = new ArrayList<>();
           
           // Filter sessions by status
           for (SessionDTO session : allSessions) {
               if (session.getStatus() != null) {
                   if (session.getStatus().equals("PENDING")) {
                       pendingSessions.add(session);
                   } else if (session.getStatus().equals("COMPLETED")) {
                       completedSessions.add(session);
                   }
               }
           }
           
           // Add all required attributes to the model
           model.addAttribute("tutor", tutor);
           model.addAttribute("sessions", upcomingSessions); // Use upcoming sessions as default view
           model.addAttribute("upcomingSessions", upcomingSessions);
           model.addAttribute("pendingSessions", pendingSessions);
           model.addAttribute("completedSessions", completedSessions);
           model.addAttribute("allSessions", upcomingSessions);
           
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

        @GetMapping("/subjects/{id}/edit")
        public String showEditSubjectForm(
                Authentication auth,
                @PathVariable("id") Integer subjectId,
                Model model) {
            try {
                UserDTO user = userService.getUserByEmail(auth.getName());
                TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
                
                // Get the subject details
                TutorSubjectDTO subject = tutorService.getTutorSubject(tutor.getId(), subjectId);
                if (subject == null) {
                    throw new RuntimeException("Subject not found");
                }
                
                model.addAttribute("subject", subject);
                return "tutor/edit-subject";
                
            } catch (Exception e) {
                model.addAttribute("error", "Failed to load subject: " + e.getMessage());
                return "redirect:/tutor/subjects";
            }
        }
    
        @PostMapping("/subjects/{id}/update")
        public String updateSubject(
                Authentication auth,
                @PathVariable("id") Integer subjectId,
                @Valid @ModelAttribute TutorSubjectUpdateDTO updateDTO,
                BindingResult bindingResult,
                Model model) {
            try {
                if (bindingResult.hasErrors()) {
                    model.addAttribute("error", "Invalid input. Please check your data.");
                    return showEditSubjectForm(auth, subjectId, model);
                }
    
                UserDTO user = userService.getUserByEmail(auth.getName());
                TutorDTO tutor = tutorService.getTutorByUserId(user.getId());
                
                tutorService.updateSubject(tutor.getId(), subjectId, updateDTO);
                model.addAttribute("success", "Subject updated successfully");
                return "redirect:/tutor/subjects";
                
            } catch (Exception e) {
                model.addAttribute("error", "Failed to update subject: " + e.getMessage());
                return showEditSubjectForm(auth, subjectId, model);
            }
        }
    
        @PostMapping("/availability/{id}/delete")
    public String deleteAvailability(
            Authentication auth,
            @PathVariable("id") Integer availabilityId,
            Model model) {
        try {
            // No need to get the user data since we're just removing availability
            tutorService.removeAvailability(availabilityId);
            return "redirect:/tutor/availability";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tutor/availability";
        }
    }
}
