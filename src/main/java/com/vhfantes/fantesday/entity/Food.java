package com.vhfantes.fantesday.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "foods", uniqueConstraints = @UniqueConstraint(columnNames = { "source", "external_id" }))
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "calories_per_100g", precision = 7, scale = 2)
    private BigDecimal caloriesPer100g;

    @Column(name = "protein_per_100g", precision = 7, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(name = "carbs_per_100g", precision = 7, scale = 2)
    private BigDecimal carbsPer100g;

    @Column(name = "fat_per_100g", precision = 7, scale = 2)
    private BigDecimal fatPer100g;

    @Column(name = "fiber_per_100g", precision = 7, scale = 2)
    private BigDecimal fiberPer100g;

    private String defaultUnit;

    @Enumerated(EnumType.STRING)
    private CatalogSource source;

    // Id na fonte de origem: numero do alimento na TACO ou codigo de barras no Open Food Facts
    @Column(name = "external_id")
    private String externalId;
}
