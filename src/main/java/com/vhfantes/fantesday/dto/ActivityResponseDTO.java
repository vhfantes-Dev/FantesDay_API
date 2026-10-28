package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vhfantes.fantesday.entity.Activity;

// caloriesEstimated = true quando as calorias foram calculadas pela API (MET x peso x horas)
public record ActivityResponseDTO(
        Long id,
        Long userId,
        String activityType,
        Integer durationMinutes,
        String origin,
        String importSource,
        String externalId,
        Integer caloriesBurned,
        Boolean caloriesEstimated,
        BigDecimal met,
        LocalDate date,
        Integer xpEarned) {

    public static ActivityResponseDTO from(Activity activity) {
        return new ActivityResponseDTO(
                activity.getId(),
                activity.getUser().getId(),
                activity.getActivityType(),
                activity.getDurationMinutes(),
                activity.getOrigin(),
                activity.getImportSource(),
                activity.getExternalId(),
                activity.getCaloriesBurned(),
                activity.getCaloriesEstimated(),
                activity.getMet(),
                activity.getDate(),
                activity.getXpEarned());
    }
}
