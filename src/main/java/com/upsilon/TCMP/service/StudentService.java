package com.upsilon.TCMP.service;

import java.util.List;
import com.upsilon.TCMP.dto.StudentDTO;
import com.upsilon.TCMP.dto.TutorDTO;

public interface StudentService {
    StudentDTO createStudent(StudentDTO studentDTO);
    StudentDTO getStudentById(Integer id);
    StudentDTO getStudentByEmail(String email);
    StudentDTO getStudentByUserId(Integer userId);
    StudentDTO getStudentWithSessions(Integer userId);
    void updateStudent(StudentDTO studentDTO);
    void deleteStudent(Integer id);
    int countActiveSubjects(Integer studentId);
    List<TutorDTO> getFavoriteTutors(Integer studentId);
    void addFavoriteTutor(Integer studentId, Integer tutorId);
    void removeFavoriteTutor(Integer studentId, Integer tutorId);
    List<Integer> getEnrolledSubjectIds(Integer studentId);
    double getAverageRating(Integer studentId);
    int getCompletedSessionCount(Integer studentId);
    double getTotalPayments(Integer studentId);
    boolean hasActiveSubscription(Integer studentId);
    void updateProfile(Integer studentId, StudentDTO updateDTO);
    void updatePreferences(Integer studentId, List<String> preferences);
    boolean canBookSession(Integer studentId, Integer tutorId);
    boolean hasCompletedProfile(Integer studentId);
}