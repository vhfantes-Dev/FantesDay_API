package com.vhfantes.fantesday.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.ApplySuggestionRequestDTO;
import com.vhfantes.fantesday.dto.BodyMetricsDTO;
import com.vhfantes.fantesday.dto.NutritionGoalResponseDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.BodyMetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class BodyMetricsController {

    private final BodyMetricsService bodyMetricsService;
    private final CurrentUser currentUser;

    // TMB, gasto diario, IMC e metas de calorias sugeridas
    @GetMapping("/body-metrics")
    public BodyMetricsDTO getMetrics(@PathVariable Long userId) {
        currentUser.checkAccess(userId);
        return bodyMetricsService.getMetrics(userId);
    }

    // Grava a meta de calorias (e proteina) sugerida para o objetivo
    @PostMapping("/goals/apply-suggestion")
    public NutritionGoalResponseDTO applySuggestion(@PathVariable Long userId, @RequestBody ApplySuggestionRequestDTO dto) {
        currentUser.checkAccess(userId);
        return bodyMetricsService.applySuggestion(userId, dto.goal());
    }
}
