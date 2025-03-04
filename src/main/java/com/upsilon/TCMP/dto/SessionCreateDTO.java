package com.upsilon.TCMP.dto;

import lombok.Data;

@Data
public class SessionCreateDTO {
    private Integer studentId;
    private Integer tutorId;
    private Integer subjectId;
    private String sessionDate;
    private String startTime;
    private String endTime;
    private String notes;
}