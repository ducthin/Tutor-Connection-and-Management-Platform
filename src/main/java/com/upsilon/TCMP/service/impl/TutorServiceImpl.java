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
public class TutorServiceImpl implements TutorService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TutorServiceImpl.class);

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
    public TutorDTO getTutorById(Integer id) {
        log.info("Getting tutor by ID: {}", id);
        try {
            log.debug("Attempting to find tutor with ID: {}", id);
            Optional<Tutor> tutorOpt = tutorRepository.findByIdWithUser(id);

            if (!tutorOpt.isPresent()) {
                log.error("No tutor found with ID: {}. Database query returned no results.", id);
                throw new RuntimeException("Tutor not found");
            }
            
            Tutor tutor = tutorOpt.get();
            log.debug("Found tutor entity with ID: {}. Checking user association...", id);

            if (tutor.getUser() == null) {
                log.error("Tutor {} exists but has no associated user. This indicates data integrity issues.", id);
                throw new RuntimeException("Tutor has no associated user");
            }
            
            log.info("Successfully found tutor: {} (ID: {}) with user: {} (ID: {})",
                    tutor.getUser().getFullName(),
                    id,
                    tutor.getUser().getEmail(),
                    tutor.getUser().getId());
            return convertToTutorDTO(tutor);
        } catch (Exception e) {
            log.error("Error getting tutor {}: {}", id, e.getMessage());
            throw new RuntimeException("Error getting tutor: " + e.getMessage());
        }
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
    @Transactional(readOnly = true)
    public List<TutorDTO> getAllTutors() {
        try {
            System.out.println("Fetching all tutors from database...");
            List<Tutor> tutors = tutorRepository.findAll();
            System.out.println("Found " + tutors.size() + " tutors");

            List<TutorDTO> dtos = tutors.stream()
                    .map(tutor -> {
                        try {
                            return convertToTutorDTO(tutor);
                        } catch (Exception e) {
                            System.err.println("Error converting tutor to DTO: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            System.out.println("Converted " + dtos.size() + " tutors to DTOs");
            return dtos;
        } catch (Exception e) {
            System.err.println("Error in getAllTutors: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<TutorDTO> searchTutors(String keyword, Integer subjectId,
                                        BigDecimal minRating, BigDecimal maxRate, String sortBy) {
        try {
            log.info("Searching tutors with params - keyword: {}, subjectId: {}, minRating: {}, maxRate: {}, sortBy: {}",
                    keyword, subjectId, minRating, maxRate, sortBy);

            // First get the IDs of matching tutors
            List<Integer> tutorIds = tutorRepository.findSearchTutorIds(keyword, subjectId, minRating, maxRate);
            log.info("Found {} matching tutor IDs", tutorIds.size());

            if (tutorIds.isEmpty()) {
                return new ArrayList<>();
            }

            // Then fetch complete tutor data with all necessary relationships
            List<Tutor> tutors = tutorRepository.findTutorsWithDetails(tutorIds);
            log.info("Loaded {} tutors with full details", tutors.size());

            // Convert to DTOs
            List<TutorDTO> dtos = tutors.stream()
                    .map(this::convertToTutorDTO)
                    .collect(Collectors.toList());

            // Sort results if needed
            if (sortBy != null) {
                switch (sortBy) {
                    case "RATING":
                        dtos.sort((a, b) -> b.getRating().compareTo(a.getRating()));
                        break;
                    case "PRICE_LOW":
                        dtos.sort(Comparator.comparing(TutorDTO::getHourlyRate));
                        break;
                    case "PRICE_HIGH":
                        dtos.sort((a, b) -> b.getHourlyRate().compareTo(a.getHourlyRate()));
                        break;
                }
            }

            return dtos;
        } catch (Exception e) {
            log.error("Error searching tutors: ", e);
            throw e;
        }
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
    public List<TutorSubjectDTO> getTutorSubjects(Integer tutorId) {
        log.info("Fetching subjects for tutor ID: {}", tutorId);
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
            
            // Use a Map to keep track of subjects by name, keeping the one with highest experience
            Map<String, TutorSubjectDTO> uniqueSubjects = new HashMap<>();
            
            for (TutorSubject subject : subjects) {
                try {
                    // Verify subject data is complete
                    if (subject.getSubject() == null) {
                        System.err.println("Subject reference is null for tutor subject ID: " + subject.getId());
                        continue;
                    }
                    
                    String subjectName = subject.getSubject().getName();
                    TutorSubjectDTO existingDto = uniqueSubjects.get(subjectName);
                    
                    // If this subject doesn't exist yet, or has more experience than the existing one
                    if (existingDto == null ||
                        (subject.getExperienceYears() != null &&
                         (existingDto.getExperienceYears() == null ||
                          subject.getExperienceYears() > existingDto.getExperienceYears()))) {
                        
                        TutorSubjectDTO dto = new TutorSubjectDTO();
                        dto.setId(subject.getId());
                        dto.setTutorId(tutorId);
                        dto.setSubjectId(subject.getSubject().getId());
                        dto.setSubjectName(subjectName);
                        dto.setHourlyRate(subject.getHourlyRate());
                        dto.setExperienceYears(subject.getExperienceYears());
                        dto.setDescription(subject.getDescription());
                        dto.setActive(subject.isActive());
                        
                        System.out.println("Converting subject: " + dto.getSubjectName() +
                                         " (ID: " + dto.getId() + ")" +
                                         ", Rate: " + dto.getHourlyRate() +
                                         ", Active: " + dto.isActive());
                        
                        uniqueSubjects.put(subjectName, dto);
                    }
                } catch (Exception e) {
                    System.err.println("Error converting subject to DTO: " + e.getMessage());
                }
            }
            
            List<TutorSubjectDTO> dtos = new ArrayList<>(uniqueSubjects.values());
            System.out.println("Returning " + dtos.size() + " unique DTOs");
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

    private void updateTutorFromDTO(Tutor tutor, TutorProfileUpdateDTO updateDTO) {
        if (updateDTO.getBio() != null) {
            tutor.setBio(updateDTO.getBio().trim());
        }
        if (updateDTO.getQualifications() != null) {
            tutor.setQualifications(updateDTO.getQualifications().trim());
        }
        if (updateDTO.getSubjectsTaught() != null) {
            tutor.setSubjectsTaught(updateDTO.getSubjectsTaught().trim());
        }
        if (updateDTO.getHourlyRate() != null) {
            tutor.setHourlyRate(updateDTO.getHourlyRate());
        }

        // Update the user data
        User user = tutor.getUser();
        String originalPictureUrl = user.getProfilePictureUrl();
        log.info("Current profile picture URL: {}", originalPictureUrl);
        
        if (updateDTO.getFullName() != null && !updateDTO.getFullName().trim().isEmpty()) {
            user.setFullName(updateDTO.getFullName().trim());
        }
        if (updateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDTO.getPhoneNumber().trim());
        }

        // Always preserve the existing profile picture URL unless explicitly changed
        if (originalPictureUrl != null) {
            log.info("Preserving original profile picture URL: {}", originalPictureUrl);
            user.setProfilePictureUrl(originalPictureUrl);
        } else {
            // If no URL, try to get it from fresh user data
            User freshUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (freshUser.getProfilePictureUrl() != null) {
                log.info("Retrieved profile picture URL from database: {}", freshUser.getProfilePictureUrl());
                user.setProfilePictureUrl(freshUser.getProfilePictureUrl());
            }
        }
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    @Transactional(rollbackFor = Exception.class)
    public TutorDTO updateTutorProfile(Integer tutorId, TutorProfileUpdateDTO updateDTO) {
        validateTutorAccess(tutorId);
        
        // Load tutor with user data in one query
        Tutor tutor = tutorRepository.findByIdWithUser(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
                
        // Update both tutor and user data
        updateTutorFromDTO(tutor, updateDTO);
        
        // Handle profile picture if provided
        if (updateDTO.getProfilePicture() != null && !updateDTO.getProfilePicture().isEmpty()) {
            String pictureUrl = uploadProfilePicture(tutorId, updateDTO.getProfilePicture());
            tutor.getUser().setProfilePictureUrl(pictureUrl);
        }
        
        // Save tutor and user
        final Tutor savedTutor = tutorRepository.save(tutor);
        User savedUser = userRepository.save(tutor.getUser());
        
        log.info("Saved user profile picture URL: {}", savedUser.getProfilePictureUrl());

        // Handle subject rates if provided in a separate transaction
        if (updateDTO.getSubjectRates() != null && !updateDTO.getSubjectRates().isEmpty()) {
            updateSubjectRates(savedTutor.getId(), updateDTO.getSubjectRates());
        }
        
        return convertToTutorDTO(savedTutor);
    }

    private void updateSubjectRates(Integer tutorId, Map<Integer, Double> subjectRates) {
        subjectRates.forEach((subjectId, rate) -> {
            TutorSubject tutorSubject = tutorSubjectRepository
                    .findByTutorIdAndSubjectId(tutorId, subjectId)
                    .orElseGet(() -> {
                        Tutor tutor = tutorRepository.getReferenceById(tutorId);
                        Subject subject = subjectRepository.getReferenceById(subjectId);
                        TutorSubject newTS = new TutorSubject();
                        newTS.setTutor(tutor);
                        newTS.setSubject(subject);
                        return newTS;
                    });
            tutorSubject.setHourlyRate(rate);
            tutorSubjectRepository.save(tutorSubject);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR', 'STUDENT')")
    @Transactional(readOnly = true)
    public List<TutorAvailabilityDTO> getTutorAvailability(Integer tutorId) {
        log.info("Fetching availability for tutor ID: {}", tutorId);
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        
        List<TutorAvailability> availabilities = availabilityRepository.findByTutor(tutor);
        log.info("Found {} availability slots for tutor {}", availabilities.size(), tutorId);
        
        return availabilities.stream()
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
    public List<ReviewDTO> getTutorReviews(Integer tutorId) {
        return reviewRepository.findByTutorId(tutorId).stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getRecentTutorReviews(Integer tutorId, int limit) {
        return reviewRepository.findRecentReviewsByTutorId(tutorId, limit).stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
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
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthorized = auth != null &&
                (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                 (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TUTOR")) &&
                  tutor.getUser().getEmail().equals(auth.getName())));

            TutorDTO dto = new TutorDTO();
            dto.setId(tutor.getId());

            // Handle user data
            User user = tutor.getUser();
            if (user != null) {
                dto.setUser(convertToUserDTO(user));
                dto.setIsVerified(user.getIsActive());
            } else {
                log.warn("Tutor {} has no associated user", tutor.getId());
                UserDTO defaultUser = new UserDTO();
                defaultUser.setFullName("Unknown User");
                defaultUser.setActive(false);
                dto.setUser(defaultUser);
                dto.setIsVerified(false);
            }

            // Set basic tutor info with null checks
            dto.setQualifications(tutor.getQualifications() != null ? tutor.getQualifications() : "");
            dto.setBio(tutor.getBio() != null ? tutor.getBio() : "");
            dto.setHourlyRate(tutor.getHourlyRate() != null ? tutor.getHourlyRate() : 0.0);
            
            try {
                // Get active subjects using optimized query
                List<TutorSubject> activeSubjects = tutorSubjectRepository.findActiveByTutorId(tutor.getId())
                        .stream()
                        .filter(ts -> ts.getSubject() != null && ts.getSubject().isActive())
                        .collect(Collectors.toList());
                
                // Create comma-separated list of subject names
                String subjectsTaught = activeSubjects.stream()
                        .map(ts -> ts.getSubject().getName())
                        .collect(Collectors.joining(", "));
                
                dto.setSubjectsTaught(subjectsTaught.isEmpty() ? "No subjects listed" : subjectsTaught);
            } catch (Exception e) {
                log.error("Error getting subjects for tutor {}: {}", tutor.getId(), e.getMessage());
                dto.setSubjectsTaught("No subjects available");
            }

            try {
                Double rating = calculateAverageRating(tutor.getId());
                dto.setRating(rating != null ? rating : 0.0);
            } catch (Exception e) {
                log.error("Error calculating rating for tutor {}: {}", tutor.getId(), e.getMessage());
                dto.setRating(0.0);
            }
            
            // Only include sensitive data for authorized users
            if (isAuthorized) {
                try {
                    dto.setTotalSessions(getTotalSessionCount(tutor.getId()).intValue());
                } catch (Exception e) {
                    log.error("Error getting session count for tutor {}: {}", tutor.getId(), e.getMessage());
                    dto.setTotalSessions(0);
                }
            } else {
                dto.setTotalSessions(0);
            }

            return dto;
        } catch (Exception e) {
            log.error("Error converting tutor to DTO: {}", e.getMessage());
            throw new RuntimeException("Failed to convert tutor to DTO", e);
        }
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
        System.out.println("Converting user to DTO - Profile picture URL: " + user.getProfilePictureUrl());
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
            System.out.println("Starting profile picture upload for tutor: " + tutorId);
            
            // Setup upload directory
            String uploadDir = "src/main/resources/static/images/profiles";
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            System.out.println("Upload path: " + uploadPath);
            
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating upload directory");
                Files.createDirectories(uploadPath);
            }

            // Get tutor and user info
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new RuntimeException("Tutor not found"));
            User user = tutor.getUser();
            System.out.println("Found tutor: " + user.getFullName());

            // Keep track of old profile picture path but don't delete it yet
            String oldPicture = user.getProfilePictureUrl();
            Path oldFilePath = null;
            if (oldPicture != null && !oldPicture.isEmpty()) {
                String oldFileName = oldPicture.substring(oldPicture.lastIndexOf("/") + 1);
                oldFilePath = uploadPath.resolve(oldFileName);
                System.out.println("Found old profile picture: " + oldFilePath);
            }

            // Validate and save new picture
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(filename);
            
            System.out.println("Saving new profile picture: " + filePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("New file saved successfully");

            // Only delete old file after new one is successfully saved
            if (oldFilePath != null && Files.exists(oldFilePath)) {
                try {
                    Files.delete(oldFilePath);
                    System.out.println("Old profile picture deleted successfully: " + oldFilePath);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to delete old profile picture: " + e.getMessage());
                    // Continue execution since new file was saved successfully
                }
            }
            
            // Update database with new profile picture URL
            String profilePictureUrl = "/images/profiles/" + filename;
            System.out.println("Updating database with new profile picture URL: " + profilePictureUrl);
            
            // Update in a new transaction to ensure changes are visible immediately
            // Update the user in a single operation
            user.setProfilePictureUrl(profilePictureUrl);
            userRepository.save(user);
            
            log.info("Updated user profile picture URL: {}", profilePictureUrl);
            log.info("Profile picture update completed successfully");
            
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