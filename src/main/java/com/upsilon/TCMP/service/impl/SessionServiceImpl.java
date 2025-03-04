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
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
            throw new IllegalArgumentException("Cannot book a session in the past. Please select a future date and time.");
        }

        // Get tutor's availability
        List<TutorAvailabilityDTO> availabilityList = tutorService.getTutorAvailability(sessionDTO.getTutor().getId());

        // Check if requested time falls within tutor's availability
        boolean isTimeAvailable = false;
        DayOfWeek sessionDay = sessionDTO.getStartTime().getDayOfWeek();

        for (TutorAvailabilityDTO availability : availabilityList) {
            if (availability.getDayOfWeek().equals(sessionDay) &&
                !sessionDTO.getStartTime().toLocalTime().isBefore(availability.getStartTime()) &&
                !sessionDTO.getEndTime().toLocalTime().isAfter(availability.getEndTime())) {
                isTimeAvailable = true;
                break;
            }
        }

        if (!isTimeAvailable) {
            throw new IllegalArgumentException("The selected time slot is not within the tutor's available hours. Please check the tutor's availability schedule and select an available time slot.");
        }

        // Check for overlapping sessions
        List<Session> existingSessions = sessionRepository.findAllByTutorId(sessionDTO.getTutor().getId());
        for (Session existingSession : existingSessions) {
            if (existingSession.getStatus() != SessionStatus.CANCELLED &&
                sessionDTO.getStartTime().isBefore(existingSession.getEndTime()) &&
                sessionDTO.getEndTime().isAfter(existingSession.getStartTime())) {
                throw new IllegalArgumentException("This time slot overlaps with another booked session. Please select a different time.");
            }
        }
    }

    @Override
    public SessionDTO createSession(SessionCreateDTO createDTO) {
        // Convert date and time strings to LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.parse(createDTO.getSessionDate() + "T" + createDTO.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.parse(createDTO.getSessionDate() + "T" + createDTO.getEndTime());

        // Create SessionDTO for validation
        SessionDTO validationDTO = new SessionDTO();
        validationDTO.setStartTime(startDateTime);
        validationDTO.setEndTime(endDateTime);
        TutorDTO tutorForValidation = new TutorDTO();
        tutorForValidation.setId(createDTO.getTutorId());
        validationDTO.setTutor(tutorForValidation);

        // Validate the time slot
        validateTimeSlot(validationDTO);

        // Create the session
        Session session = new Session();
        session.setStartTime(startDateTime);
        session.setEndTime(endDateTime);
        session.setStatus(SessionStatus.PENDING);
        session.setNotes(createDTO.getNotes());

        // Set up entities
        Student student = new Student();
        student.setId(createDTO.getStudentId());
        session.setStudent(student);

        Tutor tutor = new Tutor();
        tutor.setId(createDTO.getTutorId());
        session.setTutor(tutor);

        Subject subject = new Subject();
        subject.setId(createDTO.getSubjectId());
        session.setSubject(subject);

        return convertToDTO(sessionRepository.save(session));
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