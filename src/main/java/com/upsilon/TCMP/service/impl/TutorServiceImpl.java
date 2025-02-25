package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.*;
import com.upsilon.TCMP.entity.*;
import com.upsilon.TCMP.repository.*;
import com.upsilon.TCMP.service.TutorService;
import com.upsilon.TCMP.enums.DayOfWeek;
import com.upsilon.TCMP.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
public class TutorServiceImpl implements TutorService {

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public TutorDTO createTutor(TutorDTO tutorDTO) {
        User user = userRepository.findById(tutorDTO.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tutor tutor = new Tutor();
        tutor.setUser(user);
        tutor.setQualifications(tutorDTO.getQualifications());
        tutor.setSubjectsTaught(tutorDTO.getSubjectsTaught());
        tutor.setHourlyRate(tutorDTO.getHourlyRate());
        tutor.setBio(tutorDTO.getBio());

        return convertToTutorDTO(tutorRepository.save(tutor));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public TutorDTO getTutorByEmail(String email) {
        try {
            System.out.println("Finding tutor by email: " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            System.out.println("Found user: " + user.getEmail() + ", Role: " + user.getRole());

            Tutor tutor = tutorRepository.findByUser(user)
                    .orElseGet(() -> {
                        if (!user.getRole().equals(Role.ROLE_TUTOR)) {
                            throw new RuntimeException("User " + user.getEmail() + " is not a tutor (Role: " + user.getRole() + ")");
                        }
                        System.out.println("Creating new tutor entry for user: " + user.getEmail());
                        Tutor newTutor = new Tutor();
                        newTutor.setUser(user);
                        newTutor.setHourlyRate(0.0);
                        newTutor.setRating(0.0);
                        newTutor.setIsVerified(false);
                        return tutorRepository.save(newTutor);
                    });
            System.out.println("Returning tutor DTO for ID: " + tutor.getId());

            return convertToTutorDTO(tutor);
        } catch (Exception e) {
            System.err.println("Error in getTutorByEmail: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Map<String, Integer> getSessionStats(Integer tutorId) {
        validateTutorAccess(tutorId);
        Map<String, Integer> stats = new HashMap<>();
        
        stats.put("totalSessions", sessionRepository.countByTutor_Id(tutorId).intValue());
        stats.put("completedSessions", sessionRepository.countCompletedSessionsByTutorId(tutorId));
        stats.put("upcomingSessions", sessionRepository.countUpcomingSessionsByTutorId(tutorId));
        stats.put("cancelledSessions", sessionRepository.countCancelledSessionsByTutorId(tutorId));
        
        return stats;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Double getSessionCompletionRate(Integer tutorId) {
        validateTutorAccess(tutorId);
        int totalSessions = sessionRepository.countByTutor_Id(tutorId).intValue();
        if (totalSessions == 0) {
            return 0.0;
        }
        
        int completedSessions = sessionRepository.countCompletedSessionsByTutorId(tutorId);
        return (double) completedSessions / totalSessions * 100;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Map<String, Double> getEarningsHistory(Integer tutorId, int months) {
        validateTutorAccess(tutorId);
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        
        Map<String, Double> earningsHistory = new HashMap<>();
        for (int i = 0; i < months; i++) {
            LocalDateTime monthStart = startDate.plusMonths(i);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            
            Double earnings = paymentRepository.calculateMonthlyEarningsForTutor(
                tutorId, monthStart, monthEnd);
            
            String monthKey = monthStart.getYear() + "-" +
                            String.format("%02d", monthStart.getMonthValue());
            earningsHistory.put(monthKey, earnings != null ? earnings : 0.0);
        }
        
        return earningsHistory;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Integer getUniqueStudentCount(Integer tutorId) {
        validateTutorAccess(tutorId);
        return sessionRepository.countUniqueStudentsByTutorId(tutorId);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public List<StudentDTO> getRegularStudents(Integer tutorId) {
        validateTutorAccess(tutorId);
        return sessionRepository.findRegularStudentsByTutorId(tutorId).stream()
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public int getActiveSubjectCount(Integer tutorId) {
        validateTutorAccess(tutorId);
        return tutorSubjectRepository.countByTutorIdAndActiveTrue(tutorId);
    }

    private StudentDTO convertToStudentDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUserId(student.getUser().getId());
        dto.setFullName(student.getUser().getFullName());
        dto.setEmail(student.getUser().getEmail());
        dto.setActive(student.getUser().getIsActive());
        return dto;
    }

    private final TutorRepository tutorRepository;
    private final SessionRepository sessionRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final ReviewRepository reviewRepository;
    private final TutorAvailabilityRepository availabilityRepository;
    private final PaymentRepository paymentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Autowired
    public TutorServiceImpl(TutorRepository tutorRepository,
                          SessionRepository sessionRepository,
                          TutorSubjectRepository tutorSubjectRepository,
                          ReviewRepository reviewRepository,
                          TutorAvailabilityRepository availabilityRepository,
                          PaymentRepository paymentRepository,
                          SubjectRepository subjectRepository,
                          UserRepository userRepository) {
        this.tutorRepository = tutorRepository;
        this.sessionRepository = sessionRepository;
        this.tutorSubjectRepository = tutorSubjectRepository;
        this.reviewRepository = reviewRepository;
        this.availabilityRepository = availabilityRepository;
        this.paymentRepository = paymentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public TutorDTO getTutorById(Integer id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        return convertToTutorDTO(tutor);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public TutorDTO getTutorByUserId(Integer userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TUTOR")) &&
            !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("User is not a tutor");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Tutor tutor = tutorRepository.findByUserId(userId)
            .orElseGet(() -> {
                if (!user.getRole().equals(Role.ROLE_TUTOR)) {
                    throw new RuntimeException("User is not a tutor");
                }
                Tutor newTutor = new Tutor();
                newTutor.setUser(user);
                newTutor.setHourlyRate(0.0);
                newTutor.setRating(0.0);
                newTutor.setIsVerified(false);
                return tutorRepository.save(newTutor);
            });

        return convertToTutorDTO(tutor);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public List<TutorDTO> getAllTutors() {
        return tutorRepository.findAll().stream()
                .map(this::convertToTutorDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("permitAll")
    public List<TutorDTO> searchTutors(String keyword, Integer subjectId,
                                        BigDecimal minRating, BigDecimal maxRate, String sortBy) {
        if (sortBy != null && !List.of("RATING", "PRICE_LOW", "PRICE_HIGH").contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort criteria");
        }

        return tutorRepository.searchTutors(keyword, subjectId, minRating, maxRate, sortBy)
                .stream()
                .map(this::convertToTutorDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public List<TutorDTO> findAvailableTutors(DayOfWeek dayOfWeek, LocalTime time, Integer subjectId) {
        return tutorRepository.findAvailableTutors(dayOfWeek, time, subjectId)
                .stream()
                .map(this::convertToTutorDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public List<TutorSubjectDTO> getTutorSubjects(Integer tutorId) {
        try {
            System.out.println("Getting subjects for tutor: " + tutorId);
            
            // Get tutor first to ensure it exists
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new RuntimeException("Tutor not found"));
            System.out.println("Found tutor: " + tutor.getUser().getFullName());
            
            // Get subjects
            List<TutorSubject> subjects = tutorSubjectRepository.findByTutorId(tutorId);
            if (subjects == null) {
                System.out.println("No subjects found for tutor");
                return new ArrayList<>();
            }
            
            System.out.println("Found " + subjects.size() + " subjects");
            
            List<TutorSubjectDTO> dtos = new ArrayList<>();
            for (TutorSubject subject : subjects) {
                try {
                    // Verify subject data is complete
                    if (subject.getSubject() == null) {
                        System.err.println("Subject reference is null for tutor subject ID: " + subject.getId());
                        continue;
                    }
                    
                    TutorSubjectDTO dto = new TutorSubjectDTO();
                    dto.setId(subject.getId());
                    dto.setTutorId(tutorId);
                    dto.setSubjectId(subject.getSubject().getId());
                    dto.setSubjectName(subject.getSubject().getName());
                    dto.setHourlyRate(subject.getHourlyRate());
                    dto.setExperienceYears(subject.getExperienceYears());
                    dto.setDescription(subject.getDescription());
                    dto.setActive(subject.isActive());
                    
                    System.out.println("Converting subject: " + dto.getSubjectName() +
                                     " (ID: " + dto.getId() + ")" +
                                     ", Rate: " + dto.getHourlyRate() +
                                     ", Active: " + dto.isActive());
                    
                    dtos.add(dto);
                } catch (Exception e) {
                    System.err.println("Error converting subject to DTO: " + e.getMessage());
                }
            }
            
            System.out.println("Returning " + dtos.size() + " DTOs");
            return dtos;
        } catch (Exception e) {
            System.err.println("Error in getTutorSubjects: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public TutorSubjectDTO addSubject(Integer tutorId, TutorSubjectCreateDTO createDTO) {
        try {
            System.out.println("Adding subject for tutor: " + tutorId + ", subject: " + createDTO.getSubjectId());
            
            validateTutorAccess(tutorId);
            
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new RuntimeException("Tutor not found"));
            System.out.println("Found tutor: " + tutor.getId());
                    
            Subject subject = subjectRepository.findById(createDTO.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            System.out.println("Found subject: " + subject.getId());
            
            // Check if tutor already has this subject
            if (tutorSubjectRepository.findByTutorIdAndSubjectId(tutorId, subject.getId()).isPresent()) {
                System.out.println("Subject already exists for tutor");
                throw new RuntimeException("You have already added this subject");
            }
            
            System.out.println("Creating new tutor subject");
            TutorSubject tutorSubject = new TutorSubject();
            tutorSubject.setTutor(tutor);
            tutorSubject.setSubject(subject);

            // Rate is required as per database schema
            if (createDTO.getRate() == null) {
                throw new RuntimeException("Hourly rate is required");
            }
            System.out.println("Setting hourly rate: " + createDTO.getRate());
            tutorSubject.setHourlyRate(createDTO.getRate());
            
            if (createDTO.getExperienceYears() != null) {
                System.out.println("Setting experience years: " + createDTO.getExperienceYears());
                tutorSubject.setExperienceYears(createDTO.getExperienceYears());
            }
            
            if (createDTO.getDescription() != null) {
                tutorSubject.setDescription(createDTO.getDescription());
            }
            tutorSubject.setActive(true);
            
            System.out.println("Saving tutor subject");
            TutorSubject savedTutorSubject = tutorSubjectRepository.save(tutorSubject);
            System.out.println("Saved tutor subject with ID: " + savedTutorSubject.getId());
            
            return convertToTutorSubjectDTO(savedTutorSubject);
        } catch (Exception e) {
            System.out.println("Error adding subject: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public TutorSubjectDTO updateSubject(Integer tutorId, Integer tutorSubjectId, TutorSubjectUpdateDTO updateDTO) {
        try {
            System.out.println("Updating subject - Tutor ID: " + tutorId + ", TutorSubject ID: " + tutorSubjectId);
            validateTutorAccess(tutorId);
            
            TutorSubject tutorSubject = tutorSubjectRepository.findById(tutorSubjectId)
                    .orElseThrow(() -> new RuntimeException("TutorSubject not found"));

            // Verify tutor owns this subject
            if (!tutorSubject.getTutor().getId().equals(tutorId)) {
                throw new AccessDeniedException("Not authorized to modify this subject");
            }
            System.out.println("Found existing subject entry with ID: " + tutorSubject.getId());

            if (updateDTO.getRate() != null) {
                System.out.println("Updating rate from " + tutorSubject.getHourlyRate() + " to " + updateDTO.getRate());
                tutorSubject.setHourlyRate(updateDTO.getRate());
            }
            
            if (updateDTO.getExperienceYears() != null) {
                System.out.println("Updating experience years from " + tutorSubject.getExperienceYears() + " to " + updateDTO.getExperienceYears());
                tutorSubject.setExperienceYears(updateDTO.getExperienceYears());
            }
            
            if (updateDTO.getDescription() != null) {
                System.out.println("Updating description");
                tutorSubject.setDescription(updateDTO.getDescription());
            }
            
            if (updateDTO.getActive() != null) {
                System.out.println("Updating active status from " + tutorSubject.isActive() + " to " + updateDTO.getActive());
                tutorSubject.setActive(updateDTO.getActive());
            }

            TutorSubject savedTutorSubject = tutorSubjectRepository.save(tutorSubject);
            System.out.println("Successfully updated subject with ID: " + savedTutorSubject.getId());
            
            return convertToTutorSubjectDTO(savedTutorSubject);
        } catch (Exception e) {
            System.err.println("Error updating subject: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public void removeSubject(Integer tutorId, Integer tutorSubjectId) {
        try {
            System.out.println("Removing subject - Tutor ID: " + tutorId + ", TutorSubject ID: " + tutorSubjectId);
            validateTutorAccess(tutorId);
            
            // First verify the subject exists and belongs to the tutor
            TutorSubject subjectToDelete = tutorSubjectRepository.findById(tutorSubjectId)
                    .orElseThrow(() -> new RuntimeException("TutorSubject not found"));
            
            if (!subjectToDelete.getTutor().getId().equals(tutorId)) {
                throw new AccessDeniedException("Not authorized to delete this subject");
            }
            
            System.out.println("Found subject to delete with ID: " + subjectToDelete.getId());
            tutorSubjectRepository.deleteById(tutorSubjectId);
            System.out.println("Successfully deleted subject");
        } catch (Exception e) {
            System.err.println("Error removing subject: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void verifyTutor(Integer tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        tutor.getUser().setActive(true);
        tutorRepository.save(tutor);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public TutorDTO updateTutorProfile(Integer tutorId, TutorProfileUpdateDTO updateDTO) {
        validateTutorAccess(tutorId);
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        
        User user = tutor.getUser();
        // Update user info
        if (updateDTO.getFullName() != null) {
            user.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }

        // Update tutor info
        if (updateDTO.getBio() != null) {
            tutor.setBio(updateDTO.getBio());
        }
        if (updateDTO.getQualifications() != null) {
            tutor.setQualifications(updateDTO.getQualifications());
        }
        if (updateDTO.getSubjectsTaught() != null) {
            tutor.setSubjectsTaught(updateDTO.getSubjectsTaught());
        }
        if (updateDTO.getHourlyRate() != null) {
            tutor.setHourlyRate(updateDTO.getHourlyRate());
        }

        if (updateDTO.getSubjectRates() != null) {
            updateDTO.getSubjectRates().forEach((subjectId, rate) -> {
                TutorSubject tutorSubject = tutorSubjectRepository
                    .findByTutorIdAndSubjectId(tutorId, subjectId)
                    .orElseGet(() -> {
                        TutorSubject newTS = new TutorSubject();
                        newTS.setTutor(tutor);
                        newTS.setSubject(subjectRepository.getReferenceById(subjectId));
                        return newTS;
                    });
                tutorSubject.setHourlyRate(rate);
                tutorSubjectRepository.save(tutorSubject);
            });
        }
        
        Tutor savedTutor = tutorRepository.save(tutor);
        return convertToTutorDTO(savedTutor);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public List<TutorAvailabilityDTO> getTutorAvailability(Integer tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
                
        return availabilityRepository.findByTutor(tutor).stream()
                .map(this::convertToAvailabilityDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public List<TutorAvailabilityDTO> getTutorAvailabilityForWeek(Integer tutorId, LocalDateTime weekStart) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        LocalDateTime weekEnd = weekStart.plusDays(7);
        
        return availabilityRepository.findByTutor(tutor).stream()
                .filter(availability -> {
                    int daysDiff = availability.getDayOfWeek().ordinal() - weekStart.getDayOfWeek().getValue() + 1;
                    LocalDateTime slotDateTime = weekStart.plusDays(daysDiff)
                            .withHour(availability.getStartTime().getHour())
                            .withMinute(availability.getStartTime().getMinute());
                    return !slotDateTime.isBefore(weekStart) && !slotDateTime.isAfter(weekEnd);
                })
                .map(this::convertToAvailabilityDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public TutorAvailabilityDTO addAvailability(TutorAvailabilityCreateDTO createDTO) {
        validateTutorAccess(createDTO.getTutorId());
        Tutor tutor = tutorRepository.findById(createDTO.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
                
        TutorAvailability availability = new TutorAvailability();
        availability.setTutor(tutor);
        availability.setDayOfWeek(createDTO.getDayOfWeek());
        availability.setStartTime(createDTO.getStartTime());
        availability.setEndTime(createDTO.getEndTime());
        availability.setIsRecurring(createDTO.getIsRecurring());

        validateTimeSlots(tutor.getId(), List.of(availability));
        
        TutorAvailability savedAvailability = availabilityRepository.save(availability);
        return convertToAvailabilityDTO(savedAvailability);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public void addBulkAvailability(BulkAvailabilityCreateDTO createDTO) {
        validateTutorAccess(createDTO.getTutorId());
        Tutor tutor = tutorRepository.findById(createDTO.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        List<TutorAvailability> availabilities = createDTO.getDaysOfWeek().stream()
                .map(day -> {
                    TutorAvailability availability = new TutorAvailability();
                    availability.setTutor(tutor);
                    availability.setDayOfWeek(day);
                    availability.setStartTime(createDTO.getStartTime());
                    availability.setEndTime(createDTO.getEndTime());
                    availability.setIsRecurring(createDTO.getIsRecurring());
                    return availability;
                })
                .collect(Collectors.toList());

        validateTimeSlots(tutor.getId(), availabilities);
        availabilityRepository.saveAll(availabilities);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public void removeAvailability(Integer availabilityId) {
        TutorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));
        validateTutorAccess(availability.getTutor().getId());
        availabilityRepository.deleteById(availabilityId);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public List<SessionDTO> getTutorSessions(Integer tutorId) {
        validateTutorAccess(tutorId);
        return sessionRepository.findAllByTutorId(tutorId).stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public List<SessionDTO> getTutorUpcomingSessions(Integer tutorId) {
        validateTutorAccess(tutorId);
        return sessionRepository.findUpcomingSessionsByTutorId(tutorId).stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Long getTotalSessionCount(Integer tutorId) {
        validateTutorAccess(tutorId);
        return sessionRepository.countByTutor_Id(tutorId);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public List<ReviewDTO> getTutorReviews(Integer tutorId) {
        return reviewRepository.findByTutorId(tutorId).stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public List<ReviewDTO> getRecentTutorReviews(Integer tutorId, int limit) {
        return reviewRepository.findRecentReviewsByTutorId(tutorId, limit).stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    public Double getAverageRating(Integer tutorId) {
        return reviewRepository.getAverageRatingByTutorId(tutorId);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Double calculateTotalEarnings(Integer tutorId) {
        validateTutorAccess(tutorId);
        Double earnings = paymentRepository.calculateTotalEarningsForTutor(tutorId);
        return earnings != null ? earnings : 0.0;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public Double calculateMonthlyEarnings(Integer tutorId, int year, int month) {
        validateTutorAccess(tutorId);
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        
        Double earnings = paymentRepository.calculateMonthlyEarningsForTutor(tutorId, startDate, endDate);
        return earnings != null ? earnings : 0.0;
    }

    private void validateTutorAccess(Integer tutorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        
        if (!tutor.getUser().getEmail().equals(auth.getName())) {
            throw new AccessDeniedException("Not authorized to access this tutor's data");
        }
    }

    private void validateUserAccess(Integer userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        
        if (!tutor.getUser().getEmail().equals(auth.getName())) {
            throw new AccessDeniedException("Not authorized to access this user's data");
        }
    }

    private void validateTimeSlots(Integer tutorId, List<TutorAvailability> newSlots) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        List<TutorAvailability> existingSlots = availabilityRepository.findByTutor(tutor);
        
        for (TutorAvailability newSlot : newSlots) {
            for (TutorAvailability existingSlot : existingSlots) {
                if (newSlot.getDayOfWeek() == existingSlot.getDayOfWeek() &&
                    isTimeOverlap(newSlot.getStartTime(), newSlot.getEndTime(),
                                existingSlot.getStartTime(), existingSlot.getEndTime())) {
                    throw new RuntimeException("Time slot overlaps with existing availability");
                }
            }
        }
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    // Helper methods for DTO conversion
    private TutorDTO convertToTutorDTO(Tutor tutor) {
        TutorDTO dto = new TutorDTO();
        User user = tutor.getUser();
        dto.setId(tutor.getId());
        dto.setUser(convertToUserDTO(user));
        dto.setQualifications(tutor.getQualifications());
        dto.setSubjectsTaught(tutor.getSubjectsTaught());
        dto.setHourlyRate(tutor.getHourlyRate());
        dto.setIsVerified(user.getIsActive());
        dto.setBio(tutor.getBio());
        dto.setRating(calculateAverageRating(tutor.getId()));
        return dto;
    }

    private TutorAvailabilityDTO convertToAvailabilityDTO(TutorAvailability availability) {
        TutorAvailabilityDTO dto = new TutorAvailabilityDTO();
        dto.setId(availability.getId());
        dto.setTutorId(availability.getTutor().getId());
        dto.setDayOfWeek(availability.getDayOfWeek());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setIsRecurring(availability.getIsRecurring());
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setActive(user.getIsActive());
        return dto;
    }

    private Double calculateAverageRating(Integer tutorId) {
        Double avgRating = reviewRepository.getAverageRatingByTutorId(tutorId);
        return avgRating != null ? avgRating : 0.0;
    }

    private SessionDTO convertToSessionDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setStatus(session.getStatus());
        dto.setNotes(session.getNotes());
        return dto;
    }

    private ReviewDTO convertToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setSessionId(review.getSession().getId());
        dto.setStudentId(review.getStudent().getId());
        dto.setStudentName(review.getStudent().getUser().getFullName());
        dto.setTutorId(review.getTutor().getId());
        dto.setTutorName(review.getTutor().getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        Session session = review.getSession();
        if (session != null && session.getSubject() != null) {
            dto.setSubjectId(session.getSubject().getId());
            dto.setSubjectName(session.getSubject().getName());
        }
        return dto;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public String uploadProfilePicture(Integer tutorId, MultipartFile file) {
        validateTutorAccess(tutorId);
        try {
            String uploadDir = "src/main/resources/static/images/profiles";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;
            
            // Save the file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update the user's profile picture URL
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new RuntimeException("Tutor not found"));
            User user = tutor.getUser();
            String profilePictureUrl = "/images/profiles/" + filename;
            user.setProfilePictureUrl(profilePictureUrl);
            userRepository.save(user);
            
            return profilePictureUrl;
        } catch (IOException ex) {
            throw new RuntimeException("Could not upload profile picture", ex);
        }
    }

    private TutorSubjectDTO convertToTutorSubjectDTO(TutorSubject tutorSubject) {
        TutorSubjectDTO dto = new TutorSubjectDTO();
        dto.setId(tutorSubject.getId());
        dto.setTutorId(tutorSubject.getTutor().getId());
        dto.setSubjectId(tutorSubject.getSubject().getId());
        dto.setSubjectName(tutorSubject.getSubject().getName());
        dto.setHourlyRate(tutorSubject.getHourlyRate());
        dto.setExperienceYears(tutorSubject.getExperienceYears());
        dto.setDescription(tutorSubject.getDescription());
        dto.setActive(tutorSubject.isActive());
        return dto;
    }
}