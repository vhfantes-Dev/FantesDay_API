package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vhfantes.fantesday.entity.WeightEntry;

public record WeightEntryDTO(
        Long id,
        LocalDate date,
        BigDecimal weightKg) {

    public static WeightEntryDTO from(WeightEntry entry) {
        return new WeightEntryDTO(entry.getId(), entry.getDate(), entry.getWeightKg());
    }
}
