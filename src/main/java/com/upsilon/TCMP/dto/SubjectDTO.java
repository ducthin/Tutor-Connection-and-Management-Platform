package com.upsilon.TCMP.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubjectDTO {
    private Integer id;
    private String name;
    private String description;
    private String category;
    private boolean active;
    private int tutorCount;
    private double averageRate;
    private double lowestRate;
    private double highestRate;
}