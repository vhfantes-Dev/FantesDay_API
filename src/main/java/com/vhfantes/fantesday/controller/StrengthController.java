package com.vhfantes.fantesday.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.ExerciseHistoryDTO;
import com.vhfantes.fantesday.dto.LeaderboardEntryDTO;
import com.vhfantes.fantesday.dto.StrengthProfileDTO;
import com.vhfantes.fantesday.dto.StreakDTO;
import com.vhfantes.fantesday.dto.WeeklyLeaderboardEntryDTO;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.StreakService;
import com.vhfantes.fantesday.service.StrengthService;

import lombok.RequiredArgsConstructor;

// Rank de forca, recordes, streak e ranking entre usuarios (estilo Liftoff)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StrengthController {

    private static final int MAX_LEADERBOARD_SIZE = 100;

    private final StrengthService strengthService;
    private final StreakService streakService;
    private final CurrentUser currentUser;

    // Rank geral, por grupo muscular e por exercicio (com o melhor 1RM de cada um)
    @GetMapping("/users/{userId}/strength")
    public StrengthProfileDTO getProfile(@PathVariable Long userId) {
        currentUser.checkAccess(userId);
        return strengthService.getProfile(userId);
    }

    @GetMapping("/users/{userId}/exercises/{exerciseId}/history")
    public List<ExerciseHistoryDTO> history(@PathVariable Long userId, @PathVariable Long exerciseId) {
        currentUser.checkAccess(userId);
        return strengthService.history(userId, exerciseId);
    }

    @GetMapping("/users/{userId}/streak")
    public StreakDTO getStreak(@PathVariable Long userId) {
        currentUser.checkAccess(userId);
        return streakService.getStreak(userId);
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryDTO> leaderboard(@RequestParam(defaultValue = "50") int limit) {
        return strengthService.leaderboard(validateLimit(limit));
    }

    @GetMapping("/leaderboard/weekly")
    public List<WeeklyLeaderboardEntryDTO> weeklyLeaderboard(@RequestParam(defaultValue = "50") int limit) {
        return strengthService.weeklyLeaderboard(validateLimit(limit));
    }

    private int validateLimit(int limit) {
        if (limit < 1 || limit > MAX_LEADERBOARD_SIZE) {
            throw new BusinessException("limit deve estar entre 1 e " + MAX_LEADERBOARD_SIZE);
        }
        return limit;
    }
}
