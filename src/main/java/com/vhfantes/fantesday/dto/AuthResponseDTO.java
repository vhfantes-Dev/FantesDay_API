package com.vhfantes.fantesday.dto;

import java.time.Instant;

// token: JWT curto, enviado nas requisicoes como "Authorization: Bearer <token>".
// refreshToken: longo, guardado no app (ex: Keychain protegido por Face ID) e usado em /api/auth/refresh.
public record AuthResponseDTO(
        String token,
        String tokenType,
        Instant expiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserResponseDTO user) {
}
