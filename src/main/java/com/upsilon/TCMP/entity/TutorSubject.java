package com.upsilon.TCMP.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tutor_subjects")
public class TutorSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private boolean active = true;
}