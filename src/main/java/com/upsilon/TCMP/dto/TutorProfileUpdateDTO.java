package com.upsilon.TCMP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfileUpdateDTO {
    private String fullName;
    private String phoneNumber;
    private String bio;
    private MultipartFile profilePicture;
    private String qualifications;
    private String subjectsTaught;
    private Double hourlyRate;
    private Map<Integer, Double> subjectRates;
}