package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.util.List;

import com.vhfantes.fantesday.entity.ActivityLevel;
import com.vhfantes.fantesday.entity.Sex;

// Medidas e calculos corporais. Os calculos ficam null enquanto faltar algum dado (veja missingFields).
// bmr = taxa metabolica basal (kcal/dia); tdee = gasto calorico diario (TMB x nivel de atividade).
public record BodyMetricsDTO(
        BigDecimal heightCm,
        BigDecimal weightKg,
        Integer age,
        Sex sex,
        ActivityLevel activityLevel,
        Double activityFactor,
        Double bmi,
        String bmiCategory,
        Integer bmr,
        Integer tdee,
        CalorieSuggestions suggestedCalories,
        Integer suggestedProteinG,
        boolean complete,
        List<String> missingFields) {

    // Meta diaria sugerida para cada objetivo: perder (-500 kcal, com piso), manter (= TDEE), ganhar (+300 kcal)
    public record CalorieSuggestions(
            int lose,
            int maintain,
            int gain) {
    }
}
