package com.vhfantes.fantesday.dto;

public record ChangePasswordRequestDTO(
        String currentPassword,
        String newPassword) {
}
