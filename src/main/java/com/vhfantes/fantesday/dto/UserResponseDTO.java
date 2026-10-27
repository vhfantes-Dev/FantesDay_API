package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vhfantes.fantesday.entity.Sex;
import com.vhfantes.fantesday.entity.User;

// Dados que a API devolve: nunca expor o passwordHash
public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Boolean faceIdEnabled,
        BigDecimal bodyweightKg,
        Sex sex,
        LocalDateTime createdAt) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getFaceIdEnabled(),
                user.getBodyweightKg(),
                user.getSex(),
                user.getCreatedAt());
    }
}
