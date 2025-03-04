package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.SubjectDTO;
import com.upsilon.TCMP.dto.TutorDTO;
import com.upsilon.TCMP.dto.UserDTO;
import com.upsilon.TCMP.dto.TutorSubjectDTO;
import com.upsilon.TCMP.entity.Subject;
import com.upsilon.TCMP.entity.Tutor;
import com.upsilon.TCMP.entity.TutorSubject;
import com.upsilon.TCMP.repository.SubjectRepository;
import com.upsilon.TCMP.repository.TutorRepository;
import com.upsilon.TCMP.repository.TutorSubjectRepository;
import com.upsilon.TCMP.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private TutorSubjectRepository tutorSubjectRepository;

    @Override
    public SubjectDTO createSubject(SubjectDTO subjectDTO) {
        Subject subject = new Subject();
        subject.setName(subjectDTO.getName());
        subject.setDescription(subjectDTO.getDescription());
        subject.setCategory(subjectDTO.getCategory());
        subject.setActive(true); // New subjects are active by default
        return convertToDTO(subjectRepository.save(subject));
    }

    @Override
    public SubjectDTO getSubjectById(Integer subjectId) {
        return subjectRepository.findById(subjectId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public SubjectDTO updateSubject(Integer subjectId, SubjectDTO subjectDTO) {
        return subjectRepository.findById(subjectId)
                .map(subject -> {
                    subject.setName(subjectDTO.getName());
                    subject.setDescription(subjectDTO.getDescription());
                    subject.setCategory(subjectDTO.getCategory());
                    subject.setActive(subjectDTO.isActive());
                    return convertToDTO(subjectRepository.save(subject));
                })
                .orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    @Override
    public void deleteSubject(Integer subjectId) {
        subjectRepository.deleteById(subjectId);
    }

    @Override
    public List<SubjectDTO> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .filter(Subject::isActive)  // Only return active subjects
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectDTO> getSubjectsByCategory(String category) {
        return subjectRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return subjectRepository.findAllCategories();
    }

    @Override
    public List<SubjectDTO> searchSubjects(String keyword) {
        return subjectRepository.searchSubjects(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TutorDTO> getTutorsBySubject(Integer subjectId) {
        List<Tutor> tutors = tutorRepository.findByTutorSubjectsSubjectId(subjectId);
        return tutors.stream()
                .map(this::convertToTutorDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TutorSubjectDTO> getTutorSubjects(Integer subjectId) {
        List<TutorSubject> tutorSubjects = tutorSubjectRepository.findBySubjectId(subjectId);
        return tutorSubjects.stream()
                .map(this::convertToTutorSubjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRateForSubject(Integer subjectId) {
        return tutorSubjectRepository.getAverageHourlyRateForSubject(subjectId);
    }

    @Override
    public Map<String, Long> getSubjectEnrollmentStats() {
        List<Object[]> stats = subjectRepository.getTutorCountsBySubject();
        Map<String, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            result.put((String) stat[0], (Long) stat[1]);
        }
        return result;
    }

    @Override
    public Map<String, Double> getAverageRatingsBySubject() {
        // Implementation for average ratings (reviews)
        return new HashMap<>(); // TODO: Implement when review system is ready
    }

    @Override
    public Map<String, Double> getAverageRatesBySubject() {
        List<Object[]> rates = subjectRepository.getAverageRatesBySubject();
        Map<String, Double> result = new HashMap<>();
        for (Object[] rate : rates) {
            result.put((String) rate[0], (Double) rate[1]);
        }
        return result;
    }

    @Override
    public List<SubjectDTO> getMostPopularSubjects(int limit) {
        // TODO: Implement based on enrollment or view counts
        return new ArrayList<>();
    }

    @Override
    public List<SubjectDTO> getHighestRatedSubjects(int limit) {
        // TODO: Implement when review system is ready
        return new ArrayList<>();
    }

    @Override
    public void addCategory(String category) {
        // Categories are handled as strings in Subject entities
        // No specific implementation needed as categories are created with subjects
    }

    @Override
    public void updateCategory(String oldCategory, String newCategory) {
        List<Subject> subjects = subjectRepository.findByCategory(oldCategory);
        subjects.forEach(subject -> {
            subject.setCategory(newCategory);
            subjectRepository.save(subject);
        });
    }

    @Override
    public void deleteCategory(String category) {
        // Optional: Implement category deletion logic
        // Note: This might need careful consideration regarding existing subjects
    }

    @Override
    public void reorderCategories(List<String> orderedCategories) {
        // Optional: Implement if category ordering is needed
    }
@Override
public boolean isSubjectActive(Integer subjectId) {
    return subjectRepository.findById(subjectId)
            .map(Subject::isActive)
            .orElse(false);
}

@Override
    public boolean hasActiveTutors(Integer subjectId) {
        return subjectRepository.countVerifiedTutorsBySubject(subjectId) > 0;
    }

    @Override
    public boolean hasActiveStudents(Integer subjectId) {
        // TODO: Implement when enrollment system is ready
        return false;
    }

    // Helper methods for DTO conversion
    private SubjectDTO convertToDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setCategory(subject.getCategory());
        dto.setActive(subject.isActive());
        
        try {
            // Calculate additional fields
            Long tutorCount = subjectRepository.countVerifiedTutorsBySubject(subject.getId());
            dto.setTutorCount(tutorCount != null ? tutorCount.intValue() : 0);
            
            // Set rates
            List<TutorSubject> tutorSubjects = tutorSubjectRepository.findBySubjectId(subject.getId());
            if (!tutorSubjects.isEmpty()) {
                double avgRate = tutorSubjects.stream()
                        .mapToDouble(TutorSubject::getHourlyRate)
                        .average()
                        .orElse(0.0);
                double minRate = tutorSubjects.stream()
                        .mapToDouble(TutorSubject::getHourlyRate)
                        .min()
                        .orElse(0.0);
                double maxRate = tutorSubjects.stream()
                        .mapToDouble(TutorSubject::getHourlyRate)
                        .max()
                        .orElse(0.0);
                
                dto.setAverageRate(avgRate);
                dto.setLowestRate(minRate);
                dto.setHighestRate(maxRate);
            } else {
                dto.setAverageRate(0.0);
                dto.setLowestRate(0.0);
                dto.setHighestRate(0.0);
            }
        } catch (Exception e) {
            // Set default values if there's an error calculating statistics
            dto.setTutorCount(0);
            dto.setAverageRate(0.0);
            dto.setLowestRate(0.0);
            dto.setHighestRate(0.0);
        }
        
        return dto;
    }

    private TutorDTO convertToTutorDTO(Tutor tutor) {
        TutorDTO dto = new TutorDTO();
        dto.setId(tutor.getId());
        
        // Create and set UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(tutor.getUser().getId());
        userDTO.setFullName(tutor.getUser().getFullName());
        dto.setUser(userDTO);
        
        // Set other tutor information if needed
        dto.setQualifications(tutor.getQualifications());
        dto.setBio(tutor.getBio());
        dto.setIsVerified(tutor.getIsVerified());
        
        return dto;
    }

    private TutorSubjectDTO convertToTutorSubjectDTO(TutorSubject tutorSubject) {
        TutorSubjectDTO dto = new TutorSubjectDTO();
        dto.setId(tutorSubject.getId());
        dto.setHourlyRate(tutorSubject.getHourlyRate());
        dto.setExperienceYears(tutorSubject.getExperienceYears());
        dto.setDescription(tutorSubject.getDescription());
        return dto;
    }
}