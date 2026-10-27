package com.vhfantes.fantesday.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String muscleGroup;

    // Musculos secundarios separados por virgula
    private String secondaryMuscles;

    private String equipment;
    private String level;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private CatalogSource source;

    @Column(unique = true)
    private String externalId;
}
