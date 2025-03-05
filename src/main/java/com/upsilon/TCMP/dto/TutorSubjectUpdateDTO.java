package com.upsilon.TCMP.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TutorSubjectUpdateDTO {
    private Integer id;
    private Integer tutorId;

    @Min(value = 0, message = "Experience years must be non-negative")
    private Integer experienceYears;

    @NotNull(message = "Rate is required")
    @Min(value = 0, message = "Rate must be non-negative")
    private Double rate;

    private String description;
    private Boolean active;

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTutorId() {
        return tutorId;
    }

    public void setTutorId(Integer tutorId) {
        this.tutorId = tutorId;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}