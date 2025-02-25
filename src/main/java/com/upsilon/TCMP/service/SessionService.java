package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.SessionDTO;
import com.upsilon.TCMP.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {
    SessionDTO createSession(SessionDTO sessionDTO);
    
    SessionDTO getSessionById(Integer id);
    
    List<SessionDTO> getSessionsByTutorId(Integer tutorId);
    
    List<SessionDTO> getSessionsByStudentId(Integer studentId);
    
    void updateSession(SessionDTO sessionDTO);
    
    void deleteSession(Integer id);
    
    // Session counting and filtering methods
    long countSessionsBetween(LocalDateTime start, LocalDateTime end);
    
    long countSessionsByStatus(SessionStatus status);
    
    long countSessionsByTutorId(Integer tutorId);
    
    long countSessionsByStudentId(Integer studentId);
    
    // Session retrieval methods with filters
    List<SessionDTO> findSessionsBetween(LocalDateTime start, LocalDateTime end);
    
    List<SessionDTO> findUpcomingSessionsByTutorId(Integer tutorId);
    
    List<SessionDTO> findUpcomingSessionsByStudentId(Integer studentId);
    
    List<SessionDTO> findCompletedSessionsByTutorId(Integer tutorId);
    
    List<SessionDTO> findCompletedSessionsByStudentId(Integer studentId);
    
    // Status management
    void confirmSession(Integer sessionId);
    
    void cancelSession(Integer sessionId, String reason);
    
    void completeSession(Integer sessionId);
    
    // Analytics methods
    double getAverageSessionDuration(Integer tutorId);
    
    int getSessionCountForPeriod(Integer tutorId, LocalDateTime start, LocalDateTime end);
    
    double getCompletionRate(Integer tutorId);
}