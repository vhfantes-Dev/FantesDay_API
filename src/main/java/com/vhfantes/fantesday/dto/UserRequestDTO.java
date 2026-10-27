package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.Sex;

// Dados que chegam na API para criar/atualizar um usuario
public record UserRequestDTO(
        String name,
        String email,
        String password,
        Boolean faceIdEnabled,
        BigDecimal bodyweightKg,
        Sex sex) {
}
