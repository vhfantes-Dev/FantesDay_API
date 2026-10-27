package com.vhfantes.fantesday.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activities",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "import_source", "external_id" }))
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String activityType;

    private Integer durationMinutes;
    private String origin;
    private String importSource;
    private Integer caloriesBurned;
    private LocalDate date;

    // XP dado pela atividade (calculado pela duracao)
    private Integer xpEarned = 0;

    // Id da atividade no app de origem (Apple Saude, Google Fit...), para nao importar duas vezes
    @Column(name = "external_id")
    private String externalId;
}
