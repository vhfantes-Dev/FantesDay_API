package com.vhfantes.fantesday.dto;

import java.util.List;

// Na criacao, exercises (opcional) ja monta a rotina na ordem enviada; no PATCH so name e notes sao usados
public record RoutineRequestDTO(
        String name,
        String notes,
        List<RoutineExerciseRequestDTO> exercises) {
}
