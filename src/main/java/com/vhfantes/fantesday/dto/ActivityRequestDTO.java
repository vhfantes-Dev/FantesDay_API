package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

public record ActivityRequestDTO(
        String activityType,
        Integer durationMinutes,
        String origin,
        String importSource,
        Integer caloriesBurned,
        LocalDate date) {
}
