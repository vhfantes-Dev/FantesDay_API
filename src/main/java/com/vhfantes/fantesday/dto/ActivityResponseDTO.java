package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

import com.vhfantes.fantesday.entity.Activity;

public record ActivityResponseDTO(
        Long id,
        Long userId,
        String activityType,
        Integer durationMinutes,
        String origin,
        String importSource,
        String externalId,
        Integer caloriesBurned,
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
                activity.getDate(),
                activity.getXpEarned());
    }
}
