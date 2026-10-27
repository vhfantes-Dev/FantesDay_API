package com.vhfantes.fantesday.dto;

import java.util.List;

// skipped = atividades que ja tinham sido importadas antes (mesmo source + externalId)
public record ActivityImportResultDTO(
        int imported,
        int skipped,
        int xpEarned,
        List<ActivityResponseDTO> activities) {
}
