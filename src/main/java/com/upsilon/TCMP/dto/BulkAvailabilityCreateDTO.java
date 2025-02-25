package com.upsilon.TCMP.dto;

import com.upsilon.TCMP.enums.DayOfWeek;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class BulkAvailabilityCreateDTO {
    private Integer tutorId;
    private List<DayOfWeek> daysOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isRecurring = true;
}