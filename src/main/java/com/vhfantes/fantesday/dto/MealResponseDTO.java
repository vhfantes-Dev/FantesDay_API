package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.vhfantes.fantesday.entity.Meal;

public record MealResponseDTO(
        Long id,
        Long userId,
        String name,
        LocalDate date,
        LocalDateTime createdAt) {

    public static MealResponseDTO from(Meal meal) {
        return new MealResponseDTO(
                meal.getId(),
                meal.getUser().getId(),
                meal.getName(),
                meal.getDate(),
                meal.getCreatedAt());
    }
}
