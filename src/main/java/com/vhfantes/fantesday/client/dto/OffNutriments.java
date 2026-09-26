package com.vhfantes.fantesday.client.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Nutrientes por 100g retornados pelo Open Food Facts
@JsonIgnoreProperties(ignoreUnknown = true)
public record OffNutriments(
        @JsonProperty("energy-kcal_100g") BigDecimal energyKcal100g,
        @JsonProperty("proteins_100g") BigDecimal proteins100g,
        @JsonProperty("carbohydrates_100g") BigDecimal carbohydrates100g,
        @JsonProperty("fat_100g") BigDecimal fat100g,
        @JsonProperty("fiber_100g") BigDecimal fiber100g) {
}
