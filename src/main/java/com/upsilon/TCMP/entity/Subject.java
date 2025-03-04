package com.upsilon.TCMP.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String category;
    
    @Column(name = "is_active")
    private boolean active = true;
}
