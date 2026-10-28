package com.vhfantes.fantesday.activity;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Gasto calorico estimado pela formula do MET: kcal = MET x peso (kg) x horas
public final class ActivityCalories {

    // Usado quando o usuario ainda nao informou o peso
    public static final BigDecimal DEFAULT_WEIGHT_KG = BigDecimal.valueOf(70);

    public static final BigDecimal MIN_MET = BigDecimal.ONE;
    public static final BigDecimal MAX_MET = BigDecimal.valueOf(20);

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    private ActivityCalories() {
    }

    public static Integer estimate(BigDecimal met, BigDecimal weightKg, Integer durationMinutes) {
        if (met == null || durationMinutes == null || durationMinutes <= 0) {
            return null;
        }
        BigDecimal weight = weightKg != null ? weightKg : DEFAULT_WEIGHT_KG;
        return met.multiply(weight)
                .multiply(BigDecimal.valueOf(durationMinutes))
                .divide(MINUTES_PER_HOUR, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
