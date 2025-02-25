package com.upsilon.TCMP.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Integer id;
    private Integer sessionId;
    private Integer studentId;
    private String studentName;
    private Integer tutorId;
    private String tutorName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Integer subjectId;
    private String subjectName;
}