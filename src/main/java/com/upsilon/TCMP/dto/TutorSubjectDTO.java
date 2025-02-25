package com.upsilon.TCMP.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TutorSubjectDTO {
    private Integer id;
    private Integer tutorId;
    private Integer subjectId;
    private String subjectName;
    private Double hourlyRate;
    private Integer experienceYears;
    private String description;
    private boolean active;
}