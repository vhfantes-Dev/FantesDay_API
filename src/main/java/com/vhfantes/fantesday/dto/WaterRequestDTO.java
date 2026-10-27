package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

// Sem data, registra para hoje
public record WaterRequestDTO(
        Integer amountMl,
        LocalDate date) {
}
