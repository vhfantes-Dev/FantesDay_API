package com.vhfantes.fantesday.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.WaterDayDTO;
import com.vhfantes.fantesday.dto.WaterRequestDTO;
import com.vhfantes.fantesday.dto.WeightEntryDTO;
import com.vhfantes.fantesday.dto.WeightHistoryDTO;
import com.vhfantes.fantesday.dto.WeightRequestDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.WaterService;
import com.vhfantes.fantesday.service.WeightService;

import lombok.RequiredArgsConstructor;

// Agua bebida e historico de peso corporal
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrackingController {

    private final WaterService waterService;
    private final WeightService weightService;
    private final CurrentUser currentUser;

    // ---------- Agua ----------

    // Registra um copo/garrafa e devolve o total do dia
    @PostMapping("/users/{userId}/water")
    @ResponseStatus(HttpStatus.CREATED)
    public WaterDayDTO addWater(@PathVariable Long userId, @RequestBody WaterRequestDTO dto) {
        currentUser.checkAccess(userId);
        return waterService.add(userId, dto);
    }

    // ?date=yyyy-MM-dd (padrao: hoje)
    @GetMapping("/users/{userId}/water")
    public WaterDayDTO getWater(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        currentUser.checkAccess(userId);
        return waterService.getDay(userId, date != null ? date : LocalDate.now());
    }

    @DeleteMapping("/water/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWater(@PathVariable Long id) {
        waterService.delete(id);
    }

    // ---------- Peso ----------

    // Registra o peso do dia (substitui se ja houver) e atualiza o peso do perfil
    @PostMapping("/users/{userId}/weights")
    @ResponseStatus(HttpStatus.CREATED)
    public WeightEntryDTO addWeight(@PathVariable Long userId, @RequestBody WeightRequestDTO dto) {
        currentUser.checkAccess(userId);
        return weightService.add(userId, dto);
    }

    // ?from=yyyy-MM-dd&to=yyyy-MM-dd (padrao: ultimos 90 dias)
    @GetMapping("/users/{userId}/weights")
    public WeightHistoryDTO getWeights(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        currentUser.checkAccess(userId);
        return weightService.history(userId, from, to);
    }

    @DeleteMapping("/weights/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWeight(@PathVariable Long id) {
        weightService.delete(id);
    }
}
