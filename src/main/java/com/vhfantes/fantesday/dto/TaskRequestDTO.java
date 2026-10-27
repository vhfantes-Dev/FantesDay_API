package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TaskRequestDTO(
        String title,
        String type,
        LocalDate date,
        LocalTime time,
        String weekDays) {
}
