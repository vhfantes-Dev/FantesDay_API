package com.vhfantes.fantesday.entity;

import java.time.LocalDate;
import java.time.LocalTime;

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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private String type;

    // Tarefa avulsa: dia em que deve ser feita (null = vale ate ser concluida).
    // Tarefa recorrente: a partir de quando passa a valer (null = desde sempre).
    private LocalDate date;

    private LocalTime time;

    // Dias da semana em que a tarefa se repete, ex: "SEG,QUA,SEX" (null = tarefa avulsa).
    // As conclusoes ficam em TaskCompletion, uma por dia.
    private String weekDays;
}
