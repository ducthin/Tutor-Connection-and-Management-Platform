package com.upsilon.TCMP.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Tutors")
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tutor_id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "favoriteTutors")
    private List<Student> favoriteByStudents = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<TutorSubject> tutorSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<TutorAvailability> availabilities = new ArrayList<>();

    @Column
    private String qualifications;

    private String subjectsTaught;

    @Column(columnDefinition = "DECIMAL(10,2)")
    private Double hourlyRate;

    @Column(nullable = false)
    private Boolean isVerified = false;

    private String bio;

    @Column(columnDefinition = "DECIMAL(3,2)")
    private Double rating;
    
    @PrePersist
    public void prePersist() {
        if (isVerified == null) {
            isVerified = false;
        }
        if (rating == null) {
            rating = 0.0;
        }
    }
}
