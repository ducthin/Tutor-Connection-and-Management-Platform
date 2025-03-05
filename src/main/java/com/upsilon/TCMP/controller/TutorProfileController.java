package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import com.upsilon.TCMP.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/tutors/profile")
public class TutorProfileController {
    
    private static final Logger log = LoggerFactory.getLogger(TutorProfileController.class);
    
    @Autowired
    private TutorService tutorService;
    
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;
    
    @GetMapping("/{id}")
    public String showTutorProfile(@PathVariable("id") Integer tutorId, Authentication auth, Model model) {
        log.info("Showing tutor profile for ID: {} - User authenticated: {}", tutorId, auth != null ? auth.getName() : "anonymous");
        
        try {
            TutorDTO tutor = tutorService.getTutorById(tutorId);
            
            if (tutor == null) {
                log.error("Tutor with ID {} not found", tutorId);
                model.addAttribute("error", "Không tìm thấy gia sư với ID này");
                return "error/not-found";
            }
            
            log.debug("Retrieved tutor: {}", tutor);
            
            // Đảm bảo tutor.user không null
            if (tutor.getUser() == null) {
                log.error("Tutor {} exists but has no associated user information", tutorId);
                model.addAttribute("error", "Thông tin người dùng của gia sư không tồn tại");
                return "error/not-found";
            }
            
            // Lấy thông tin liên quan
            List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
            List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
            List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutorId, 5);
            Double averageRating = tutorService.getAverageRating(tutorId);
            
            // Kiểm tra xem đây có phải là tutor đang xem profile của chính họ không
            boolean isTutorProfile = false;
            boolean isInFavorites = false;
            
            if (auth != null) {
                UserDTO user = userService.getUserByEmail(auth.getName());
                TutorDTO currentTutor = tutorService.getTutorByUserId(user.getId());
                isTutorProfile = (currentTutor != null && currentTutor.getId().equals(tutorId));
                
                // Nếu người xem là học sinh, kiểm tra xem họ có thể đặt lịch không
                if (!isTutorProfile) {
                    try {
                        StudentDTO student = studentService.getStudentByEmail(auth.getName());
                        boolean canBook = studentService.canBookSession(student.getId(), tutorId);
                        model.addAttribute("canBook", canBook);
                        if (!canBook) {
                            model.addAttribute("warning", "Bạn cần có gói đăng ký hoạt động để đặt buổi học.");
                        }
                        
                        // Kiểm tra xem tutor có trong danh sách yêu thích không
                        List<TutorDTO> favorites = studentService.getFavoriteTutors(student.getId());
                        isInFavorites = favorites.stream()
                                .anyMatch(favTutor -> favTutor.getId().equals(tutorId));
                    } catch (Exception e) {
                        // Không phải học sinh, bỏ qua
                        log.debug("User is not a student: {}", e.getMessage());
                    }
                }
            }
            
            log.debug("isTutorProfile: {}, isInFavorites: {}", isTutorProfile, isInFavorites);
            
            // Truyền dữ liệu vào model
            model.addAttribute("tutor", tutor);
            model.addAttribute("tutorSubjects", tutorSubjects);
            model.addAttribute("availability", availability);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
            model.addAttribute("isTutorProfile", isTutorProfile);
            model.addAttribute("isInFavorites", isInFavorites);
            
            log.info("Successfully loaded tutor profile page for tutor ID: {}", tutorId);
            return "profile/tutor";
        } catch (Exception e) {
            log.error("Error showing tutor profile for ID {}: {}", tutorId, e.getMessage(), e);
            model.addAttribute("error", "Đã xảy ra lỗi khi hiển thị hồ sơ gia sư: " + e.getMessage());
            return "error/server-error";
        }
    }
    
    @GetMapping("/view-old-profile/{id}")
    public String viewTutorProfile(@PathVariable("id") Integer tutorId) {
        log.info("Redirecting from /view-old-profile/{} to /profile/{}", tutorId, tutorId);
        return "redirect:/tutors/profile/" + tutorId;
    }
} 