package com.upsilon.TCMP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorSubjectUpdateDTO {
    private Double rate;
    private Integer experienceYears;
    private String description;
    private Boolean active;
}