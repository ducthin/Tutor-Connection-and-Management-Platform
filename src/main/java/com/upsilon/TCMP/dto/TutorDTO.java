package com.upsilon.TCMP.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TutorDTO {
    private Integer id;
    private UserDTO user;
    private String qualifications;
    private String subjectsTaught;
    private Double hourlyRate;
    private Boolean isVerified;
    private String bio;
    private Double rating;
    private Integer totalSessions; // Added field for total sessions
}