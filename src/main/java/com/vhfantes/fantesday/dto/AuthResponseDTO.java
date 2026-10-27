package com.vhfantes.fantesday.dto;

import java.time.Instant;

// Envie o token nas proximas requisicoes: "Authorization: Bearer <token>"
public record AuthResponseDTO(
        String token,
        String tokenType,
        Instant expiresAt,
        UserResponseDTO user) {
}
