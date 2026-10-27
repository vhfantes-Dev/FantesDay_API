package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.util.List;

// Lote de atividades vindas de outro app (ex: source = "APPLE_HEALTH", "GOOGLE_FIT").
// O app le os dados do celular e envia aqui; externalId evita importar a mesma atividade duas vezes.
public record ActivityImportRequestDTO(
        String source,
        List<Item> activities) {

    public record Item(
            String externalId,
            String activityType,
            Integer durationMinutes,
            Integer caloriesBurned,
            LocalDate date) {
    }
}
