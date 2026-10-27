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
        Integer caloriesBurned,
        LocalDate date) {

    public static ActivityResponseDTO from(Activity activity) {
        return new ActivityResponseDTO(
                activity.getId(),
                activity.getUser().getId(),
                activity.getActivityType(),
                activity.getDurationMinutes(),
                activity.getOrigin(),
                activity.getImportSource(),
                activity.getCaloriesBurned(),
                activity.getDate());
    }
}
