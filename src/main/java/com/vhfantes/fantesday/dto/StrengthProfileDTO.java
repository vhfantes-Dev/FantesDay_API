package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.util.List;

import com.vhfantes.fantesday.entity.Sex;

// Rank geral, por grupo muscular e por exercicio. overall e null enquanto nao houver exercicio com rank.
public record StrengthProfileDTO(
        Long userId,
        BigDecimal bodyweightKg,
        Sex sex,
        RankDTO overall,
        List<MuscleRankDTO> muscles,
        List<ExerciseRankDTO> exercises) {
}
