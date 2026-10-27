package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

// Sem data, o treino e criado para hoje
public record StartRoutineRequestDTO(
        LocalDate date) {
}
