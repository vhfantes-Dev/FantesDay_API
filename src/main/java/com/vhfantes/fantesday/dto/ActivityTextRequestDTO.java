package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

// Atividade descrita em texto livre, ex: { "text": "joguei volei por 2 horas" }
public record ActivityTextRequestDTO(
        String text,
        LocalDate date) {
}
