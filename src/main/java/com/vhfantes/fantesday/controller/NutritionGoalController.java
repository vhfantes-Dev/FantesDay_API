package com.vhfantes.fantesday.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.DailyNutritionDTO;
import com.vhfantes.fantesday.dto.NutritionGoalRequestDTO;
import com.vhfantes.fantesday.dto.NutritionGoalResponseDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.NutritionGoalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class NutritionGoalController {

    private final NutritionGoalService nutritionGoalService;
    private final CurrentUser currentUser;

    @GetMapping("/goals")
    public NutritionGoalResponseDTO findGoals(@PathVariable Long userId) {
        currentUser.checkAccess(userId);
        return nutritionGoalService.findByUser(userId);
    }

    // Cria ou atualiza as metas (so os campos enviados)
    @PatchMapping("/goals")
    public NutritionGoalResponseDTO updateGoals(@PathVariable Long userId, @RequestBody NutritionGoalRequestDTO dto) {
        currentUser.checkAccess(userId);
        return nutritionGoalService.update(userId, dto);
    }

    // Consumo do dia vs. meta; ?date=yyyy-MM-dd (padrao: hoje)
    @GetMapping("/nutrition/daily")
    public DailyNutritionDTO getDaily(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        currentUser.checkAccess(userId);
        return nutritionGoalService.getDaily(userId, date != null ? date : LocalDate.now());
    }
}
