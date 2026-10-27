package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

public record ExerciseRankDTO(
        Long exerciseId,
        String exerciseName,
        String muscleGroup,
        BigDecimal bestOneRepMax,
        BigDecimal bodyweightRatio,
        RankDTO rank) {
}
