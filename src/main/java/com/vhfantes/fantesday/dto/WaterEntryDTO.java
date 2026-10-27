package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.vhfantes.fantesday.entity.WaterEntry;

public record WaterEntryDTO(
        Long id,
        LocalDate date,
        Integer amountMl,
        LocalDateTime createdAt) {

    public static WaterEntryDTO from(WaterEntry entry) {
        return new WaterEntryDTO(entry.getId(), entry.getDate(), entry.getAmountMl(), entry.getCreatedAt());
    }
}
