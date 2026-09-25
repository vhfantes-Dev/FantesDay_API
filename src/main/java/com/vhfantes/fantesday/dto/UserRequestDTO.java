package com.vhfantes.fantesday.dto;

// Dados que chegam na API para criar/atualizar um usuario
public record UserRequestDTO(
        String name,
        String email,
        String password,
        Boolean faceIdEnabled) {
}
