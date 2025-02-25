package com.upsilon.TCMP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorSubjectCreateDTO {
    private Integer tutorId;
    
    @NotNull(message = "Subject is required")
    private Integer subjectId;
    
    @NotNull(message = "Hourly rate is required")
    @Positive(message = "Hourly rate must be greater than zero")
    private Double rate;
    
    private Integer experienceYears;
    private String description;
}