package com.vhfantes.fantesday.dto;

// targetReps e uma faixa, ex: "8-12". position (opcional, comeca em 0) move o exercicio de lugar.
public record RoutineExerciseRequestDTO(
        Long exerciseId,
        Integer targetSets,
        String targetReps,
        Integer restSeconds,
        Integer position) {
}
