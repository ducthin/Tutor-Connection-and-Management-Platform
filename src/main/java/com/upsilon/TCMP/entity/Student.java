package com.upsilon.TCMP.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "learning_preferences", columnDefinition = "TEXT")
    private String learningPreferences;
    
    @Column(name = "subjects_of_interest", columnDefinition = "TEXT")
    private String subjectsOfInterest;
    
    @Column(name = "has_subscription")
    private Boolean hasSubscription = false;
    
    @ManyToMany
    @JoinTable(
        name = "student_favorite_subjects",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> favoriteSubjects = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "student_favorite_tutors",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "tutor_id")
    )
    private List<Tutor> favoriteTutors = new ArrayList<>();
    
    @OneToMany(mappedBy = "student")
    private List<Session> sessions = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLearningPreferences() {
        return learningPreferences;
    }

    public void setLearningPreferences(String learningPreferences) {
        this.learningPreferences = learningPreferences;
    }

    public String getSubjectsOfInterest() {
        return subjectsOfInterest;
    }

    public void setSubjectsOfInterest(String subjectsOfInterest) {
        this.subjectsOfInterest = subjectsOfInterest;
    }

    public Boolean getHasSubscription() {
        return hasSubscription;
    }

    public void setHasSubscription(Boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
    }

    public List<Subject> getFavoriteSubjects() {
        return favoriteSubjects;
    }

    public void setFavoriteSubjects(List<Subject> favoriteSubjects) {
        this.favoriteSubjects = favoriteSubjects;
    }

    public List<Tutor> getFavoriteTutors() {
        return favoriteTutors;
    }

    public void setFavoriteTutors(List<Tutor> favoriteTutors) {
        this.favoriteTutors = favoriteTutors;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
}