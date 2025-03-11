package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.service.SessionService;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.service.SubjectService;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/book")
@PreAuthorize("hasRole('STUDENT')")
public class SessionBookingController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @GetMapping("/tutor/{id}")
    public String showBookingForm(@PathVariable("id") Integer tutorId, Model model) {
        // Get tutor information
        TutorDTO tutor = tutorService.getTutorById(tutorId);
        if (tutor == null) {
            model.addAttribute("error", "Gia sư không tồn tại");
            return "tutor/book-session";
        }

        // Get tutor's subjects
        List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(tutorId);
        if (tutorSubjects.isEmpty()) {
            model.addAttribute("error", "Gia sư này chưa có môn học nào. Vui lòng yêu cầu họ thêm môn học trước khi đặt lịch.");
            model.addAttribute("tutor", tutor);
            return "tutor/book-session";
        }

        // Get tutor's availability
        List<TutorAvailabilityDTO> originalAvailability = tutorService.getTutorAvailability(tutorId);
        if (originalAvailability.isEmpty()) {
            // Không còn yêu cầu gia sư phải có lịch rảnh, chỉ hiển thị thông báo
            model.addAttribute("warning", "Gia sư này chưa thiết lập thời gian rảnh. Bạn vẫn có thể đề xuất thời gian học phù hợp với mình.");
            model.addAttribute("tutor", tutor);
            model.addAttribute("tutorSubjects", tutorSubjects);
            // Tạo danh sách rỗng để tránh lỗi
            model.addAttribute("availability", new ArrayList<FormattedAvailabilityDTO>());
            return "tutor/book-session";
        }

        // Format time to h:mm a format (e.g., 9:00 AM)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        List<FormattedAvailabilityDTO> formattedAvailability = new ArrayList<>();
        
        for (TutorAvailabilityDTO slot : originalAvailability) {
            FormattedAvailabilityDTO formattedSlot = new FormattedAvailabilityDTO();
            formattedSlot.setId(slot.getId());
            formattedSlot.setTutorId(slot.getTutorId());
            formattedSlot.setDayOfWeek(slot.getDayOfWeek());
            formattedSlot.setStartTime(slot.getStartTime().format(timeFormatter));
            formattedSlot.setEndTime(slot.getEndTime().format(timeFormatter));
            formattedSlot.setIsRecurring(slot.getIsRecurring());
            formattedAvailability.add(formattedSlot);
        }

        // Add all necessary data to the model
        model.addAttribute("tutor", tutor);
        model.addAttribute("tutorSubjects", tutorSubjects);
        model.addAttribute("availability", formattedAvailability);

        return "tutor/book-session";
    }

    // Lớp DTO tạm thời để lưu trữ availability đã được định dạng
    public static class FormattedAvailabilityDTO {
        private Integer id;
        private Integer tutorId;
        private com.upsilon.TCMP.enums.DayOfWeek dayOfWeek;
        private String startTime;
        private String endTime;
        private Boolean isRecurring;

        // Getters and setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public Integer getTutorId() { return tutorId; }
        public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }
        
        public com.upsilon.TCMP.enums.DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(com.upsilon.TCMP.enums.DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        
        public Boolean getIsRecurring() { return isRecurring; }
        public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }
    }

    @PostMapping("/session")
    public String bookSession(
            @RequestParam("tutorId") Integer tutorId,
            @RequestParam("subjectId") Integer subjectId,
            @RequestParam("sessionDate") String sessionDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Get current student
            UserDTO user = userService.getUserByEmail(auth.getName());
            StudentDTO student = studentService.getStudentByUserId(user.getId());

            // Tạo bộ ghi log chi tiết để theo dõi và gỡ lỗi
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== THÔNG TIN ĐẶT LỊCH ===\n");
            debugInfo.append("- Ngày: ").append(sessionDate).append("\n");
            debugInfo.append("- Giờ bắt đầu: ").append(startTime).append("\n");
            debugInfo.append("- Giờ kết thúc: ").append(endTime).append("\n");
            
            // Chuẩn hóa định dạng thời gian
            debugInfo.append("- Thời gian trước khi chuẩn hóa: startTime=[" + startTime + "], endTime=[" + endTime + "]\n");
            
            // Loại bỏ T nếu có
            if (startTime.contains("T")) {
                startTime = startTime.substring(startTime.indexOf("T") + 1);
            }
            
            if (endTime.contains("T")) {
                endTime = endTime.substring(endTime.indexOf("T") + 1);
            }
            
            // Đảm bảo định dạng thời gian đúng (HH:MM)
            if (startTime.length() <= 5 && !startTime.contains(":")) {
                // Nếu chỉ có giờ, thêm phút
                startTime = startTime + ":00";
            }
            
            if (endTime.length() <= 5 && !endTime.contains(":")) {
                // Nếu chỉ có giờ, thêm phút
                endTime = endTime + ":00";
            }
            
            debugInfo.append("- Thời gian sau khi chuẩn hóa: startTime=[" + startTime + "], endTime=[" + endTime + "]\n");
            
            debugInfo.append("- Sau khi xử lý T: startTime=[").append(startTime).append("], endTime=[").append(endTime).append("]\n");
            
            // Hiển thị thông tin về ngày đã chọn
            try {
                java.time.LocalDate parsedDate = java.time.LocalDate.parse(sessionDate);
                java.time.DayOfWeek dayOfWeek = parsedDate.getDayOfWeek();
                debugInfo.append("- Ngày được chọn: ").append(parsedDate).append(" (").append(dayOfWeek).append(")\n");
                
                // Ánh xạ sang tên tiếng Việt để dễ đọc
                String viDay = "";
                switch (dayOfWeek) {
                    case MONDAY: viDay = "Thứ Hai"; break;
                    case TUESDAY: viDay = "Thứ Ba"; break;
                    case WEDNESDAY: viDay = "Thứ Tư"; break;
                    case THURSDAY: viDay = "Thứ Năm"; break;
                    case FRIDAY: viDay = "Thứ Sáu"; break;
                    case SATURDAY: viDay = "Thứ Bảy"; break;
                    case SUNDAY: viDay = "Chủ Nhật"; break;
                }
                debugInfo.append("- Tên ngày tiếng Việt: ").append(viDay).append("\n");
                
                // Nếu có thể, thử tạo LocalDateTime để xem cách xử lý
                try {
                    String fullStartDateTime = sessionDate + "T" + startTime;
                    String fullEndDateTime = sessionDate + "T" + endTime;
                    debugInfo.append("- Thử tạo DateTime đầy đủ cho giờ bắt đầu: ").append(fullStartDateTime).append("\n");
                    debugInfo.append("- Thử tạo DateTime đầy đủ cho giờ kết thúc: ").append(fullEndDateTime).append("\n");
                } catch (Exception e) {
                    debugInfo.append("- Lỗi khi thử tạo DateTime đầy đủ: ").append(e.getMessage()).append("\n");
                }
            } catch (Exception e) {
                debugInfo.append("- Lỗi khi phân tích ngày: ").append(e.getMessage()).append("\n");
            }
            
            // Hiển thị thông tin về các lịch rảnh của gia sư
            List<TutorAvailabilityDTO> availability = tutorService.getTutorAvailability(tutorId);
            debugInfo.append("\n=== LỊCH RẢNH CỦA GIA SƯ ===\n");
            for (TutorAvailabilityDTO slot : availability) {
                debugInfo.append("- ").append(slot.getDayOfWeek()).append(": ");
                debugInfo.append(slot.getStartTime()).append(" - ").append(slot.getEndTime()).append("\n");
                debugInfo.append("  (ID: ").append(slot.getId()).append(", Tuần lặp lại: ").append(slot.getIsRecurring()).append(")\n");
            }

            // Create DTO
            SessionCreateDTO createDTO = new SessionCreateDTO();
            createDTO.setStudentId(student.getId());
            createDTO.setTutorId(tutorId);
            createDTO.setSubjectId(subjectId);
            createDTO.setSessionDate(sessionDate);
            createDTO.setStartTime(startTime);
            createDTO.setEndTime(endTime);
            createDTO.setNotes(notes);
            
            debugInfo.append("\n=== DỮ LIỆU GỬI ĐẾN SERVICE ===\n");
            debugInfo.append("- StudentId: ").append(student.getId()).append("\n");
            debugInfo.append("- TutorId: ").append(tutorId).append("\n");
            debugInfo.append("- SubjectId: ").append(subjectId).append("\n");
            debugInfo.append("- SessionDate: ").append(sessionDate).append("\n");
            debugInfo.append("- StartTime: ").append(startTime).append("\n");
            debugInfo.append("- EndTime: ").append(endTime).append("\n");
            debugInfo.append("- Notes: ").append(notes != null ? notes : "").append("\n");

            try {
                // Create session
                debugInfo.append("\nBắt đầu tạo phiên...\n");
                SessionDTO sessionDTO = sessionService.createSession(createDTO);
                debugInfo.append("Đã tạo phiên thành công với ID: ").append(sessionDTO.getId()).append("\n");

                // Lấy thông tin chi tiết về buổi học đã đặt
                TutorDTO tutor = tutorService.getTutorById(tutorId);
                SubjectDTO subject = subjectService.getSubjectById(subjectId);
                String successMessage = "Bạn đã đặt lịch học môn " + subject.getName() + 
                                        " với gia sư " + tutor.getUser().getFullName() + 
                                        " vào ngày " + sessionDate + " lúc " + startTime + " thành công!";

                // Success - redirect to student dashboard
                redirectAttributes.addFlashAttribute("success", successMessage);
                return "redirect:/dashboard/student";
            } catch (Exception e) {
                // Lưu thông tin gỡ lỗi vào thông báo lỗi
                String errorMessage = "Lỗi khi đặt lịch học: " + e.getMessage();
                
                // Thêm thông tin về stack trace nếu cần
                debugInfo.append("\n=== THÔNG TIN LỖI CHI TIẾT ===\n");
                debugInfo.append("- Lỗi: ").append(e.getMessage()).append("\n");
                
                // Lấy 3 dòng đầu tiên của stack trace để dễ gỡ lỗi
                StackTraceElement[] stack = e.getStackTrace();
                if (stack != null && stack.length > 0) {
                    debugInfo.append("- Stack trace:\n");
                    for (int i = 0; i < Math.min(3, stack.length); i++) {
                        debugInfo.append("  ").append(stack[i]).append("\n");
                    }
                }
                
                redirectAttributes.addFlashAttribute("error", errorMessage);
                redirectAttributes.addFlashAttribute("debugInfo", debugInfo.toString());
                return "redirect:/book/tutor/" + tutorId;
            }
        } catch (Exception e) {
            // Đảm bảo thông báo lỗi được hiển thị bằng tiếng Việt
            String errorMessage = e.getMessage();
            
            // Dịch các thông báo lỗi tiếng Anh cụ thể sang tiếng Việt
            if (errorMessage != null) {
                if (errorMessage.contains("The selected time slot is not within the tutor's available hours")) {
                    errorMessage = errorMessage.replace(
                        "The selected time slot is not within the tutor's available hours. Please check the tutor's availability schedule and select an available time slot.",
                        "Khung giờ đã chọn không nằm trong thời gian rảnh của gia sư. Vui lòng kiểm tra lịch rảnh của gia sư và chọn khung giờ phù hợp."
                    );
                }
                
                // Thêm các thông báo lỗi tiếng Anh khác nếu cần
                if (errorMessage.contains("Invalid time format")) {
                    errorMessage = errorMessage.replace(
                        "Invalid time format",
                        "Định dạng thời gian không hợp lệ"
                    );
                }
                
                if (errorMessage.contains("Cannot parse")) {
                    errorMessage = "Không thể phân tích thông tin ngày và giờ. Vui lòng kiểm tra lại định dạng.";
                }
            }
            
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đặt lịch học: " + errorMessage);
            return "redirect:/book/tutor/" + tutorId;
        }
    }
}