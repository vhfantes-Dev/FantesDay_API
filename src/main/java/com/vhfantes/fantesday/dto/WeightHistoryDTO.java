package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// change = ultimo peso do periodo - primeiro peso do periodo (negativo = perdeu peso)
public record WeightHistoryDTO(
        LocalDate from,
        LocalDate to,
        BigDecimal currentWeightKg,
        BigDecimal changeKg,
        List<WeightEntryDTO> entries) {
}
