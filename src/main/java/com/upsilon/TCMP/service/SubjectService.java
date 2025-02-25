package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.SubjectDTO;
import com.upsilon.TCMP.dto.TutorDTO;
import com.upsilon.TCMP.dto.TutorSubjectDTO;
import java.util.List;
import java.util.Map;

public interface SubjectService {
    // Subject CRUD operations
    SubjectDTO createSubject(SubjectDTO subjectDTO);
    SubjectDTO getSubjectById(Integer subjectId);
    SubjectDTO updateSubject(Integer subjectId, SubjectDTO subjectDTO);
    void deleteSubject(Integer subjectId);
    
    // Subject queries
    List<SubjectDTO> getAllSubjects();
    List<SubjectDTO> getSubjectsByCategory(String category);
    List<String> getAllCategories();
    List<SubjectDTO> searchSubjects(String keyword);
    
    // Tutor-Subject relationships
    List<TutorDTO> getTutorsBySubject(Integer subjectId);
    List<TutorSubjectDTO> getTutorSubjects(Integer subjectId);
    Double getAverageRateForSubject(Integer subjectId);
    
    // Subject statistics
    Map<String, Long> getSubjectEnrollmentStats();
    Map<String, Double> getAverageRatingsBySubject();
    Map<String, Double> getAverageRatesBySubject();
    List<SubjectDTO> getMostPopularSubjects(int limit);
    List<SubjectDTO> getHighestRatedSubjects(int limit);
    
    // Category management
    void addCategory(String category);
    void updateCategory(String oldCategory, String newCategory);
    void deleteCategory(String category);
    void reorderCategories(List<String> orderedCategories);
    
    // Subject validation
    boolean isSubjectActive(Integer subjectId);
    boolean hasActiveTutors(Integer subjectId);
    boolean hasActiveStudents(Integer subjectId);
}