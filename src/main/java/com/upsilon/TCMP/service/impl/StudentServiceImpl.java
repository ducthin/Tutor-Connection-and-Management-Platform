package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.entity.Session;
import com.upsilon.TCMP.entity.Subject;
import com.upsilon.TCMP.entity.Student;
import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.repository.StudentRepository;
import com.upsilon.TCMP.repository.TutorRepository;
import com.upsilon.TCMP.repository.UserRepository;
import com.upsilon.TCMP.service.StudentService;
import com.upsilon.TCMP.entity.Tutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = new Student();
        updateStudentFromDTO(student, studentDTO);
        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }

    @Override
    public StudentDTO getStudentById(Integer id) {
        Student student = studentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return convertToDTO(student);
    }

    @Override
    public StudentDTO getStudentByEmail(String email) {
        Student student = studentRepository.findByUserEmailWithUser(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return convertToDTO(student);
    }

    @Override
    public StudentDTO getStudentByUserId(Integer userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return convertToDTO(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDTO getStudentWithSessions(Integer userId) {
        Student student = studentRepository.findByUserIdWithSessions(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return convertToDTO(student);
    }

    @Override
    @Transactional
    public void updateStudent(StudentDTO studentDTO) {
        // Load student with user data
        Student student = studentRepository.findByIdWithUser(studentDTO.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Use the common update method to handle all fields
        updateStudentFromDTO(student, studentDTO);
        
        // Save both entities
        studentRepository.save(student);
        userRepository.save(student.getUser());
    }

    @Override
    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

    @Override
    public int countActiveSubjects(Integer studentId) {
        return studentRepository.countActiveSubjects(studentId);
    }

    @Override
    public List<TutorDTO> getFavoriteTutors(Integer studentId) {
        return studentRepository.findFavoriteTutors(studentId).stream()
                .map(this::convertTutorToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addFavoriteTutor(Integer studentId, Integer tutorId) {
        // Verify the tutor exists first
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
                
        // Check if already in favorites
        if (studentRepository.isTutorInFavorites(studentId, tutorId)) {
            throw new RuntimeException("Tutor is already in favorites");
        }
        
        // Get student with favorites loaded
        Student student = studentRepository.findByIdWithFavoriteTutors(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Add to favorites using JPA relationship
        student.getFavoriteTutors().add(tutor);
        studentRepository.save(student);
    }

    @Override
    @Transactional
    public void removeFavoriteTutor(Integer studentId, Integer tutorId) {
        // Check if actually in favorites first
        if (!studentRepository.isTutorInFavorites(studentId, tutorId)) {
            throw new RuntimeException("Tutor is not in favorites");
        }
        
        // Get student with favorites loaded
        Student student = studentRepository.findByIdWithFavoriteTutors(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Remove from favorites using JPA relationship
        student.getFavoriteTutors().removeIf(t -> t.getId().equals(tutorId));
        studentRepository.save(student);
    }

    @Override
    public List<Integer> getEnrolledSubjectIds(Integer studentId) {
        return studentRepository.findEnrolledSubjectIds(studentId);
    }

    @Override
    public double getAverageRating(Integer studentId) {
        Double rating = studentRepository.findAverageRating(studentId);
        return rating != null ? rating : 0.0;
    }

    @Override
    public int getCompletedSessionCount(Integer studentId) {
        return studentRepository.countCompletedSessions(studentId);
    }

    @Override
    public double getTotalPayments(Integer studentId) {
        Double total = studentRepository.sumTotalPayments(studentId);
        return total != null ? total : 0.0;
    }

    @Override
    public boolean hasActiveSubscription(Integer studentId) {
        return studentRepository.hasActiveSubscription(studentId);
    }

    @Override
    @Transactional
    public void updateProfile(Integer studentId, StudentDTO updateDTO) {
        Student student = studentRepository.findByIdWithUser(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
                
        // Update both student and user data
        updateStudentFromDTO(student, updateDTO);
        
        // Save both student and user changes in one transaction
        studentRepository.save(student);
        userRepository.save(student.getUser());
    }

    @Override
    public void updatePreferences(Integer studentId, List<String> preferences) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        student.setSubjectsOfInterest(String.join(",", preferences));
        studentRepository.save(student);
    }

    @Override
    public boolean canBookSession(Integer studentId, Integer tutorId) {
        return studentRepository.hasActiveSubscription(studentId) &&
               !studentRepository.hasOutstandingPayments(studentId);
    }

    @Override
    public boolean hasCompletedProfile(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getLearningPreferences() != null &&
               student.getSubjectsOfInterest() != null;
    }

    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUserId(student.getUser().getId());
        dto.setUser(convertToUserDTO(student.getUser()));
        dto.setLearningPreferences(student.getLearningPreferences());
        dto.setSubjectsOfInterest(student.getSubjectsOfInterest());
        dto.setActive(student.getUser().getIsActive());
        dto.setHasSubscription(student.getHasSubscription());
        dto.setHasCompletedProfile(student.getLearningPreferences() != null &&
                                 student.getSubjectsOfInterest() != null);
                                 
        // Set sessions if available
        if (student.getSessions() != null) {
            List<SessionDTO> sessionDTOs = student.getSessions().stream()
                .map(session -> {
                    SessionDTO sessionDTO = new SessionDTO();
                    sessionDTO.setId(session.getId());
                    sessionDTO.setStartTime(session.getStartTime());
                    sessionDTO.setEndTime(session.getEndTime());
                    sessionDTO.setStatus(session.getStatus());
                    
                    if (session.getTutor() != null) {
                        TutorDTO tutorDTO = new TutorDTO();
                        tutorDTO.setId(session.getTutor().getId());
                        tutorDTO.setUser(convertToUserDTO(session.getTutor().getUser()));
                        sessionDTO.setTutor(tutorDTO);
                    }
                    
                    if (session.getSubject() != null) {
                        SubjectDTO subjectDTO = new SubjectDTO();
                        subjectDTO.setId(session.getSubject().getId());
                        subjectDTO.setName(session.getSubject().getName());
                        sessionDTO.setSubject(subjectDTO);
                    }
                    
                    return sessionDTO;
                })
                .collect(Collectors.toList());
            dto.setSessions(sessionDTOs);
        }
        
        return dto;
    }

    private void updateStudentFromDTO(Student student, StudentDTO dto) {
        if (dto.getLearningPreferences() != null) {
            student.setLearningPreferences(dto.getLearningPreferences());
        }
        if (dto.getSubjectsOfInterest() != null) {
            student.setSubjectsOfInterest(dto.getSubjectsOfInterest());
        }
        
        // Update the user data
        if (dto.getUser() != null) {
            User user = student.getUser();
            UserDTO userDTO = dto.getUser();
            
            if (userDTO.getFullName() != null) {
                user.setFullName(userDTO.getFullName());
            }
            if (userDTO.getPhoneNumber() != null) {
                user.setPhoneNumber(userDTO.getPhoneNumber());
            }
            if (userDTO.getProfilePictureUrl() != null) {
                user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
            }
        }
    }

    private TutorDTO convertTutorToDTO(Tutor tutor) {
        TutorDTO dto = new TutorDTO();
        dto.setId(tutor.getId());
        dto.setUser(convertToUserDTO(tutor.getUser()));
        dto.setBio(tutor.getBio());
        dto.setQualifications(tutor.getQualifications());
        dto.setSubjectsTaught(tutor.getSubjectsTaught());
        
        // Set rating with default to 0.0
        Double avgRating = tutorRepository.getAverageRating(tutor.getId());
        dto.setRating(avgRating != null ? avgRating : 0.0);
        
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }
}