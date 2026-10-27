package com.vhfantes.fantesday.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Metas diarias de alimentacao do usuario (1:1, mesmo padrao do Progress)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nutrition_goals")
public class NutritionGoal {
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private Integer dailyCalories;

    @Column(precision = 6, scale = 2)
    private BigDecimal proteinG;

    @Column(precision = 6, scale = 2)
    private BigDecimal carbsG;

    @Column(precision = 6, scale = 2)
    private BigDecimal fatG;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
