package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.entity.*;
import com.upsilon.TCMP.enums.SessionStatus;
import com.upsilon.TCMP.repository.SessionRepository;
import com.upsilon.TCMP.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final StudentService studentService;
    private final TutorService tutorService;
    private final SubjectService subjectService;

    @Autowired
    public SessionServiceImpl(
        SessionRepository sessionRepository,
        StudentService studentService,
        TutorService tutorService,
        SubjectService subjectService
    ) {
        this.sessionRepository = sessionRepository;
        this.studentService = studentService;
        this.tutorService = tutorService;
        this.subjectService = subjectService;
    }

    @Override
    public void validateTimeSlot(SessionDTO sessionDTO) {
        // Validate future date
        if (sessionDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể đặt lịch cho thời gian trong quá khứ. Vui lòng chọn ngày và giờ trong tương lai.");
        }
        // Không còn kiểm tra lịch rảnh của gia sư, thay vào đó chỉ ghi log thông tin
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("Thông tin chi tiết:\n");
        
        java.time.DayOfWeek javaDayOfWeek = sessionDTO.getStartTime().getDayOfWeek();
        com.upsilon.TCMP.enums.DayOfWeek sessionDay = mapJavaDayOfWeekToAppDayOfWeek(javaDayOfWeek);
        
        LocalTime requestedStartTime = sessionDTO.getStartTime().toLocalTime();
        LocalTime requestedEndTime = sessionDTO.getEndTime().toLocalTime();
        
        debugInfo.append("Thời gian yêu cầu: ").append(javaDayOfWeek).append(" (").append(sessionDay).append(")\n");
        debugInfo.append("Giờ bắt đầu: ").append(requestedStartTime).append(" (").append(requestedStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))).append(")\n");
        debugInfo.append("Giờ kết thúc: ").append(requestedEndTime).append(" (").append(requestedEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))).append(")\n");
        debugInfo.append("Ngày và giờ đầy đủ: ").append(sessionDTO.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" đến ").append(sessionDTO.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        // Kiểm tra thời gian học hợp lý (ít nhất 30 phút, không quá 4 giờ)
        Duration duration = Duration.between(requestedStartTime, requestedEndTime);
        long minutes = duration.toMinutes();
        
        if (minutes < 30) {
            throw new IllegalArgumentException("Thời gian học phải kéo dài ít nhất 30 phút.");
        }
        
        if (minutes > 240) { // 4 giờ
            throw new IllegalArgumentException("Thời gian học không được vượt quá 4 giờ.");
        }

        // Check for overlapping sessions
        List<Session> existingSessions = sessionRepository.findAllByTutorId(sessionDTO.getTutor().getId());
        for (Session existingSession : existingSessions) {
            if (existingSession.getStatus() != SessionStatus.CANCELLED &&
                sessionDTO.getStartTime().isBefore(existingSession.getEndTime()) &&
                sessionDTO.getEndTime().isAfter(existingSession.getStartTime())) {
                throw new IllegalArgumentException("Khung giờ này trùng với một buổi học đã được đặt. Vui lòng chọn thời gian khác.");
            }
        }
    }

    /**
     * Chuyển đổi từ java.time.DayOfWeek sang enum DayOfWeek của ứng dụng
     */
    private com.upsilon.TCMP.enums.DayOfWeek mapJavaDayOfWeekToAppDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        switch (javaDayOfWeek) {
            case MONDAY: return com.upsilon.TCMP.enums.DayOfWeek.MONDAY;
            case TUESDAY: return com.upsilon.TCMP.enums.DayOfWeek.TUESDAY;
            case WEDNESDAY: return com.upsilon.TCMP.enums.DayOfWeek.WEDNESDAY;
            case THURSDAY: return com.upsilon.TCMP.enums.DayOfWeek.THURSDAY;
            case FRIDAY: return com.upsilon.TCMP.enums.DayOfWeek.FRIDAY;
            case SATURDAY: return com.upsilon.TCMP.enums.DayOfWeek.SATURDAY;
            case SUNDAY: return com.upsilon.TCMP.enums.DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Không thể chuyển đổi ngày: " + javaDayOfWeek);
        }
    }

    @Override
    public SessionDTO createSession(SessionCreateDTO createDTO) {
        try {
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== THÔNG TIN TẠO PHIÊN HỌC ===\n");
            
            // Ghi log thông tin nhận được
            debugInfo.append("Thông tin nhận được:\n");
            debugInfo.append("- Ngày: ").append(createDTO.getSessionDate()).append("\n");
            debugInfo.append("- Giờ bắt đầu: ").append(createDTO.getStartTime()).append("\n");
            debugInfo.append("- Giờ kết thúc: ").append(createDTO.getEndTime()).append("\n");

            // Parse date and times
            LocalDateTime startDateTime = parseDateTime(createDTO.getSessionDate(), createDTO.getStartTime(), debugInfo);
            LocalDateTime endDateTime = parseDateTime(createDTO.getSessionDate(), createDTO.getEndTime(), debugInfo);
            
            debugInfo.append("Đã phân tích thành công:\n");
            debugInfo.append("- Thời gian bắt đầu: ").append(startDateTime).append("\n");
            debugInfo.append("- Thời gian kết thúc: ").append(endDateTime).append("\n");

            // Create session DTO for validation
            SessionDTO sessionDTO = new SessionDTO();
            sessionDTO.setStartTime(startDateTime);
            sessionDTO.setStudent(studentService.getStudentById(createDTO.getStudentId()));
            sessionDTO.setTutor(tutorService.getTutorById(createDTO.getTutorId()));
            sessionDTO.setSubject(subjectService.getSubjectById(createDTO.getSubjectId()));
            sessionDTO.setStartTime(startDateTime);
            sessionDTO.setEndTime(endDateTime);
            sessionDTO.setNotes(createDTO.getNotes());
            
            // Calculate price
            List<TutorSubjectDTO> tutorSubjects = tutorService.getTutorSubjects(createDTO.getTutorId());
            TutorSubjectDTO tutorSubject = tutorSubjects.stream()
                .filter(ts -> ts.getSubjectId().equals(createDTO.getSubjectId()))
                .findFirst()
                .orElse(null);

            if (tutorSubject == null) {
                throw new IllegalArgumentException("Gia sư không dạy môn học này");
            }

            // Calculate duration in hours based on start and end times
            Duration duration = Duration.between(startDateTime, endDateTime);
            double durationHours = duration.toMinutes() / 60.0;
            double price = tutorSubject.getHourlyRate() * durationHours;

            // Validate time slot
            validateTimeSlot(sessionDTO);
            debugInfo.append("Đã qua xác thực khung giờ thành công!\n");

            // Create session entity
            Session session = new Session();

            // Sử dụng đối tượng từ service thay vì repository
            Student student = new Student();
            student.setId(createDTO.getStudentId());
            session.setStudent(student);

            Tutor tutor = new Tutor();
            tutor.setId(createDTO.getTutorId());
            session.setTutor(tutor);

            Subject subject = new Subject();
            subject.setId(createDTO.getSubjectId());
            session.setSubject(subject);

            session.setStartTime(startDateTime);
            session.setEndTime(endDateTime);
            session.setPrice(price);
            session.setStatus(SessionStatus.PENDING);
            session.setNotes(createDTO.getNotes());

            // Save session
            session = sessionRepository.save(session);
            
            // Return DTO with correct status setter
            return convertToDTO(session);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Lỗi định dạng ngày giờ: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo phiên học: " + e.getMessage(), e);
        }
    }

    /**
     * Cải tiến phương thức phân tích ngày giờ để hỗ trợ nhiều định dạng
     */
    private LocalDateTime parseDateTime(String date, String time, StringBuilder debugInfo) {
        debugInfo.append("\nPhân tích ngày và giờ:\n");
        debugInfo.append("- Chuỗi ngày: [").append(date).append("]\n");
        debugInfo.append("- Chuỗi giờ: [").append(time).append("]\n");
        
        // Xử lý nhiều định dạng thời gian
        LocalTime localTime;
        try {
            // Chuẩn hóa chuỗi thời gian
            time = time.trim();
            
            // Thử với nhiều định dạng khác nhau
            if (time.matches("(?i).*[ap]m.*")) {  // Kiểm tra có chứa am/pm không (case insensitive)
                // Định dạng 12 giờ (9:00 AM, 1:30 PM)
                debugInfo.append("- Thử phân tích với định dạng 12 giờ (h:mm a)\n");
                // Chuẩn hóa AM/PM
                time = time.replaceAll("(?i)a\\.m\\.", "AM").replaceAll("(?i)p\\.m\\.", "PM");
                time = time.replaceAll("(?i)am", "AM").replaceAll("(?i)pm", "PM");
                try {
                    localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
                } catch (Exception e) {
                    // Thử với định dạng khác
                    localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
                }
            } else if (time.contains("T")) {
                // Định dạng ISO (T09:00:00)
                debugInfo.append("- Thử phân tích với định dạng ISO\n");
                String cleanTime = time.substring(time.indexOf('T') + 1);
                if (cleanTime.length() <= 5) {
                    localTime = LocalTime.parse(cleanTime, DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    localTime = LocalTime.parse(cleanTime);
                }
            } else if (time.contains(":")) {
                // Định dạng 24 giờ (09:00, 13:30)
                debugInfo.append("- Thử phân tích với định dạng 24 giờ (HH:mm)\n");
                if (time.length() <= 5) {
                    localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    localTime = LocalTime.parse(time);
                }
            } else {
                // Nếu là số giờ đơn giản (9, 13)
                debugInfo.append("- Thử phân tích với giờ đơn giản (H)\n");
                int hour = Integer.parseInt(time.trim());
                localTime = LocalTime.of(hour, 0);
            }
            
            debugInfo.append("- Đã phân tích thành công giờ: ").append(localTime).append("\n");
        } catch (Exception e) {
            debugInfo.append("- Lỗi phân tích giờ: ").append(e.getMessage()).append("\n");
            throw new DateTimeParseException("Không thể phân tích giờ: " + time + ". Vui lòng sử dụng định dạng HH:MM hoặc HH:MM AM/PM", time, 0, e);
        }
        
        // Phân tích ngày
        LocalDate localDate;
        try {
            debugInfo.append("- Phân tích ngày theo định dạng ISO (yyyy-MM-dd)\n");
            date = date.trim();
            localDate = LocalDate.parse(date);
            debugInfo.append("- Đã phân tích thành công ngày: ").append(localDate).append("\n");
        } catch (Exception e) {
            debugInfo.append("- Lỗi phân tích ngày: ").append(e.getMessage()).append("\n");
            throw new DateTimeParseException("Không thể phân tích ngày: " + date + ". Vui lòng sử dụng định dạng YYYY-MM-DD", date, 0, e);
        }
        
        // Kết hợp ngày và giờ
        LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);
        debugInfo.append("- Kết quả cuối cùng: ").append(dateTime).append("\n");
        
        return dateTime;
    }

    @Override
    public SessionDTO getSessionById(Integer id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return convertToDTO(session);
    }

    @Override
    public List<SessionDTO> getSessionsByTutorId(Integer tutorId) {
        return sessionRepository.findAllByTutorId(tutorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getSessionsByStudentId(Integer studentId) {
        return sessionRepository.findAllByStudentId(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateSession(SessionDTO sessionDTO) {
        Session session = sessionRepository.findById(sessionDTO.getId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStartTime(sessionDTO.getStartTime());
        session.setEndTime(sessionDTO.getEndTime());
        session.setNotes(sessionDTO.getNotes());
        
        sessionRepository.save(session);
    }

    @Override
    public void deleteSession(Integer id) {
        sessionRepository.deleteById(id);
    }

    @Override
    public long countSessionsBetween(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findSessionsByDateRange(start, end).size();
    }

    @Override
    public long countSessionsByStatus(SessionStatus status) {
        return sessionRepository.countByStatus(status);
    }

    @Override
    public long countSessionsByTutorId(Integer tutorId) {
        return sessionRepository.countByTutor_Id(tutorId);
    }

    @Override
    public long countSessionsByStudentId(Integer studentId) {
        return sessionRepository.findByStudent_Id(studentId).size();
    }

    @Override
    public List<SessionDTO> findSessionsBetween(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findSessionsByDateRange(start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> findUpcomingSessionsByTutorId(Integer tutorId) {
        return sessionRepository.findUpcomingSessionsByTutorId(tutorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> findUpcomingSessionsByStudentId(Integer studentId) {
        return sessionRepository.findUpcomingSessionsByStudentId(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> findCompletedSessionsByTutorId(Integer tutorId) {
        // Use count by tutor ID and status since we don't have a direct method
        return sessionRepository.findAllByTutorId(tutorId).stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> findCompletedSessionsByStudentId(Integer studentId) {
        return sessionRepository.findByStudent_IdAndStatus(studentId, SessionStatus.COMPLETED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> findPendingSessionsByTutorId(Integer tutorId) {
        return sessionRepository.findPendingSessionsByTutorId(tutorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void confirmSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus(SessionStatus.SCHEDULED);
        sessionRepository.save(session);
    }

    @Override
    public void cancelSession(Integer sessionId, String reason) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus(SessionStatus.CANCELLED);
        session.setNotes(reason);
        sessionRepository.save(session);
    }

    @Override
    public void completeSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);
    }

    @Override
    public double getAverageSessionDuration(Integer tutorId) {
        List<Session> sessions = sessionRepository.findAllByTutorId(tutorId).stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .collect(Collectors.toList());
        
        if (sessions.isEmpty()) {
            return 0.0;
        }

        double totalDuration = sessions.stream()
                .mapToLong(session -> Duration.between(session.getStartTime(), session.getEndTime()).toMinutes())
                .sum();

        return totalDuration / sessions.size();
    }

    @Override
    public int getSessionCountForPeriod(Integer tutorId, LocalDateTime start, LocalDateTime end) {
        List<Session> sessions = sessionRepository.findSessionsByDateRange(start, end);
        return (int) sessions.stream()
                .filter(session -> session.getTutor().getId().equals(tutorId))
                .count();
    }

    @Override
    public double getCompletionRate(Integer tutorId) {
        long totalSessions = sessionRepository.countByTutor_Id(tutorId);
        if (totalSessions == 0) {
            return 0.0;
        }

        long completedSessions = sessionRepository.countByTutorIdAndStatus(tutorId, SessionStatus.COMPLETED);
        return (double) completedSessions / totalSessions * 100;
    }

    private SessionDTO convertToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setStatus(session.getStatus());
        
        if (session.getStudent() != null) {
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setId(session.getStudent().getId());
            if (session.getStudent().getUser() != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(session.getStudent().getUser().getId());
                userDTO.setFullName(session.getStudent().getUser().getFullName());
                studentDTO.setUser(userDTO);
            }
            dto.setStudent(studentDTO);
        }
        
        if (session.getTutor() != null) {
            TutorDTO tutorDTO = new TutorDTO();
            tutorDTO.setId(session.getTutor().getId());
            if (session.getTutor().getUser() != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(session.getTutor().getUser().getId());
                userDTO.setFullName(session.getTutor().getUser().getFullName());
                tutorDTO.setUser(userDTO);
            }
            dto.setTutor(tutorDTO);
        }
        
        if (session.getSubject() != null) {
            SubjectDTO subjectDTO = new SubjectDTO();
            subjectDTO.setId(session.getSubject().getId());
            subjectDTO.setName(session.getSubject().getName());
            dto.setSubject(subjectDTO);
        }
        
        return dto;
    }

    public Map<String, Long> getStudentSubjectHoursDistribution(Integer studentId) {
        List<Object[]> results = sessionRepository.getStudentSubjectHoursDistribution(studentId);
        Map<String, Long> distribution = new HashMap<>();
        
        for (Object[] result : results) {
            String subject = (String) result[0];
            Long hours = ((Number) result[1]).longValue();
            distribution.put(subject, hours);
        }
        
        return distribution;
    }
}