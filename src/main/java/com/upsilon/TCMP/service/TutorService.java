package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.enums.DayOfWeek;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface TutorService {
    TutorDTO createTutor(TutorDTO tutorDTO);
    
    TutorDTO getTutorById(Integer id);
    
    TutorDTO getTutorByUserId(Integer userId);
    
    TutorDTO getTutorByEmail(String email);
    
    List<TutorDTO> getAllTutors();
    
    TutorDTO updateTutorProfile(Integer tutorId, TutorProfileUpdateDTO updateDTO);
    
    void verifyTutor(Integer tutorId);
    
    // Subject management
    List<TutorSubjectDTO> getTutorSubjects(Integer tutorId);
    
    TutorSubjectDTO addSubject(Integer tutorId, TutorSubjectCreateDTO createDTO);
    
    TutorSubjectDTO updateSubject(Integer tutorId, Integer tutorSubjectId, TutorSubjectUpdateDTO updateDTO);
    
    void removeSubject(Integer tutorId, Integer tutorSubjectId);
    
    int getActiveSubjectCount(Integer tutorId);
    
    // Availability management
    List<TutorAvailabilityDTO> getTutorAvailability(Integer tutorId);
    
    TutorAvailabilityDTO addAvailability(TutorAvailabilityCreateDTO createDTO);
    
    void removeAvailability(Integer availabilityId);
    
    void addBulkAvailability(BulkAvailabilityCreateDTO createDTO);
    
    // Session statistics
    Long getTotalSessionCount(Integer tutorId);
    
    List<SessionDTO> getTutorSessions(Integer tutorId);
    
    List<SessionDTO> getTutorUpcomingSessions(Integer tutorId);
    
    Double getSessionCompletionRate(Integer tutorId);
    
    Integer getUniqueStudentCount(Integer tutorId);
    
    // Reviews and ratings
    List<ReviewDTO> getTutorReviews(Integer tutorId);
    
    List<ReviewDTO> getRecentTutorReviews(Integer tutorId, int limit);
    
    Double getAverageRating(Integer tutorId);
    
    // Earnings
    Double calculateTotalEarnings(Integer tutorId);
    
    Double calculateMonthlyEarnings(Integer tutorId, int year, int month);
    
    // Analytics
    Map<String, Double> getEarningsHistory(Integer tutorId, int months);
    
    Map<String, Integer> getSessionStats(Integer tutorId);
    
    List<StudentDTO> getRegularStudents(Integer tutorId);
    
    // Search and availability
    List<TutorDTO> findAvailableTutors(DayOfWeek dayOfWeek, LocalTime time, Integer subjectId);
    
    // Advanced search
    List<TutorDTO> searchTutors(String keyword, Integer subjectId, BigDecimal minRating, BigDecimal maxRate, String sortBy);
    
    // Profile picture management
    String uploadProfilePicture(Integer tutorId, MultipartFile file);
}