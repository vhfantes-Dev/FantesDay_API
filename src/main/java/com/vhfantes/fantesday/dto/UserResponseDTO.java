package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.vhfantes.fantesday.entity.ActivityLevel;
import com.vhfantes.fantesday.entity.Sex;
import com.vhfantes.fantesday.entity.User;

// Dados que a API devolve: nunca expor o passwordHash.
// photoUrl: caminho da foto de perfil (null se nao houver); o "?v=" muda quando a foto muda, para o app nao usar cache antigo.
public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Boolean faceIdEnabled,
        BigDecimal bodyweightKg,
        Sex sex,
        BigDecimal heightCm,
        LocalDate birthDate,
        ActivityLevel activityLevel,
        String photoUrl,
        LocalDateTime createdAt) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getFaceIdEnabled(),
                user.getBodyweightKg(),
                user.getSex(),
                user.getHeightCm(),
                user.getBirthDate(),
                user.getActivityLevel(),
                photoUrl(user),
                user.getCreatedAt());
    }

    public static String photoUrl(User user) {
        if (user.getPhotoUpdatedAt() == null) {
            return null;
        }
        return "/api/users/" + user.getId() + "/photo?v=" + user.getPhotoUpdatedAt().toEpochSecond(ZoneOffset.UTC);
    }
}
