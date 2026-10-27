package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.util.List;

// goalMl e percent ficam null se a meta de agua nao foi definida (PATCH /goals com dailyWaterMl)
public record WaterDayDTO(
        LocalDate date,
        int totalMl,
        Integer goalMl,
        Integer percent,
        List<WaterEntryDTO> entries) {
}
