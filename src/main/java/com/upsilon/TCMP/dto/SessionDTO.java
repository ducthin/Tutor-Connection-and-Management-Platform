package com.upsilon.TCMP.dto;

import java.time.LocalDateTime;
import com.upsilon.TCMP.enums.SessionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SessionDTO {
    private Integer id;
    private TutorDTO tutor;
    private StudentDTO student;
    private SubjectDTO subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status;
    private String cancellationReason;
    private String notes;
}