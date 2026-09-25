package com.vhfantes.fantesday.dto;

import java.time.LocalDateTime;

import com.vhfantes.fantesday.entity.Progress;

public record ProgressResponseDTO(
        Long userId,
        Integer totalXp,
        Integer level,
        String category,
        LocalDateTime updatedAt) {

    public static ProgressResponseDTO from(Progress progress) {
        return new ProgressResponseDTO(
                progress.getUserId(),
                progress.getTotalXp(),
                progress.getLevel(),
                progress.getCategory(),
                progress.getUpdatedAt());
    }
}
