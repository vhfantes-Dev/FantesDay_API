package com.vhfantes.fantesday.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Uma serie de um exercicio do treino (ex: 80kg x 5)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workout_sets")
public class WorkoutSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(nullable = false)
    private Integer reps;

    // Carga usada; em exercicios com peso corporal (barra fixa, paralelas) e a carga extra
    @Column(precision = 6, scale = 2)
    private BigDecimal weightKg;

    // Calculado ao registrar a serie (formula de Epley)
    @Column(name = "estimated_one_rep_max", precision = 7, scale = 2)
    private BigDecimal estimatedOneRepMax;

    // true se, quando registrada, a serie bateu o melhor 1RM do usuario no exercicio
    private Boolean personalRecord = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
