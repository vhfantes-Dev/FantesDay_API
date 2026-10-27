package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vhfantes.fantesday.entity.ActivityLevel;
import com.vhfantes.fantesday.entity.Sex;

// Dados que chegam na API para criar/atualizar um usuario.
// heightCm, birthDate e activityLevel (SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE) alimentam a TMB.
public record UserRequestDTO(
        String name,
        String email,
        String password,
        Boolean faceIdEnabled,
        BigDecimal bodyweightKg,
        Sex sex,
        BigDecimal heightCm,
        LocalDate birthDate,
        ActivityLevel activityLevel) {
}
