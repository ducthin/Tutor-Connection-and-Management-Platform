package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.DayOfWeek;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TutorAvailabilityCreateDTO {
    private Integer tutorId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isRecurring = true;
}