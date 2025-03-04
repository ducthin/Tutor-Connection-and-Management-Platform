package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.ReviewService;
import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tutors/profile")
public class TutorProfileController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/{id}")
    public String showTutorProfile(@PathVariable("id") Integer tutorId, Model model) {
        try {
            TutorDTO tutor = tutorService.getTutorById(tutorId);
            
            if (tutor == null) {
                model.addAttribute("error", "Không tìm thấy gia sư với ID này");
                return "error/not-found";
            }
            
            // Đảm bảo tutor.user không null
            if (tutor.getUser() == null) {
                model.addAttribute("error", "Thông tin người dùng của gia sư không tồn tại");
                return "error/not-found";
            }
            
            // Lấy thông tin liên quan
            List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
            List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
            List<ReviewDTO> recentReviews = tutorService.getRecentTutorReviews(tutorId, 5);
            Double averageRating = tutorService.getAverageRating(tutorId);
            
            // Truyền dữ liệu vào model
            model.addAttribute("tutor", tutor);
            model.addAttribute("tutorSubjects", tutorSubjects);
            model.addAttribute("availability", availability);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
            
            return "profile/tutor";
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi khi hiển thị hồ sơ gia sư: " + e.getMessage());
            return "error/general";
        }
    }
} 