package com.vhfantes.fantesday.dto;

public record ResetPasswordRequestDTO(
        String email,
        String code,
        String newPassword) {
}
