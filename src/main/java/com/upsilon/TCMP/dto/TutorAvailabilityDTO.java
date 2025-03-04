package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.DayOfWeek;
import java.time.LocalTime;

public class TutorAvailabilityDTO {
    private Integer id;
    private Integer tutorId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isRecurring;

    // Constructors
    public TutorAvailabilityDTO() {}

    public TutorAvailabilityDTO(Integer id, Integer tutorId, DayOfWeek dayOfWeek, 
                              LocalTime startTime, LocalTime endTime, Boolean isRecurring) {
        this.id = id;
        this.tutorId = tutorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
    }

    // Getters and Setters
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

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    @Override
    public String toString() {
        return "TutorAvailabilityDTO{" +
                "id=" + id +
                ", tutorId=" + tutorId +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isRecurring=" + isRecurring +
                '}';
    }
}