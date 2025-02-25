package com.upsilon.TCMP.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentDTO {
    private Integer id;
    private Integer userId;
    private UserDTO user;
    private String fullName;
    private String email;
    private String learningPreferences;
    private String subjectsOfInterest;
    private List<SessionDTO> sessions;
    private List<Integer> favoriteSubjects;
    private List<Integer> favoriteTutorIds;
    private boolean isActive;
    private boolean hasSubscription;
    private boolean hasCompletedProfile;
}