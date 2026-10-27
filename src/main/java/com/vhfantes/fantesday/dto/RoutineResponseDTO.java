package com.vhfantes.fantesday.dto;

import java.time.LocalDateTime;
import java.util.List;

// id e null em rotinas geradas com save = false (so uma sugestao, nao foi salva)
public record RoutineResponseDTO(
        Long id,
        Long userId,
        String name,
        String notes,
        List<RoutineExerciseResponseDTO> exercises,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
