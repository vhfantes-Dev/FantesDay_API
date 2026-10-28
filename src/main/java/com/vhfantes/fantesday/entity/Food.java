package com.vhfantes.fantesday.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    // "g" ou "ml" (liquidos)
    private String defaultUnit;

    // Liquidos podem ser registrados em ml; os valores nutricionais continuam por 100 g
    @Column(nullable = false)
    private Boolean liquid = false;

    // Densidade em g/ml para converter ml em gramas (null = 1,0, como a agua)
    @Column(name = "density_g_per_ml", precision = 5, scale = 3)
    private BigDecimal densityGPerMl;

    // Porcao tipica (ex: 211 g e "1 sanduiche" para o Big Mac); null quando nao ha uma porcao padrao
    @Column(name = "serving_grams", precision = 7, scale = 1)
    private BigDecimal servingGrams;

    @Column(name = "serving_description", length = 100)
    private String servingDescription;

    @Enumerated(EnumType.STRING)
    private CatalogSource source;

    // Id na fonte de origem: numero do alimento na TACO ou codigo de barras no Open Food Facts
    @Column(name = "external_id")
    private String externalId;

    // Quem cadastrou (so em alimentos CUSTOM); so essa pessoa pode alterar ou excluir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
