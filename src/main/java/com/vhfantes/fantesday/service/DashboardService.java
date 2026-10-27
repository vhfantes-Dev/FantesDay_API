package com.vhfantes.fantesday.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.BodyMetricsDTO;
import com.vhfantes.fantesday.dto.DailyNutritionDTO;
import com.vhfantes.fantesday.dto.DashboardDTO;
import com.vhfantes.fantesday.dto.ProgressResponseDTO;
import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.dto.WeightEntryDTO;
import com.vhfantes.fantesday.dto.WeightHistoryDTO;
import com.vhfantes.fantesday.dto.WorkoutResponseDTO;
import com.vhfantes.fantesday.entity.Activity;
import com.vhfantes.fantesday.repository.ActivityRepository;
import com.vhfantes.fantesday.repository.WorkoutRepository;

import lombok.RequiredArgsConstructor;

// Junta os dados da tela inicial em uma chamada: perfil, XP, streak, tarefas, alimentacao, saldo de calorias,
// agua, atividades, treinos, metricas corporais (TMB/IMC), peso e rank de forca
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int WEIGHT_WINDOW_DAYS = 30;

    private final UserService userService;
    private final ProgressService progressService;
    private final StreakService streakService;
    private final TaskService taskService;
    private final NutritionGoalService nutritionGoalService;
    private final WaterService waterService;
    private final StrengthService strengthService;
    private final BodyMetricsService bodyMetricsService;
    private final WeightService weightService;
    private final ActivityRepository activityRepository;
    private final WorkoutRepository workoutRepository;

    @Transactional(readOnly = true)
    public DashboardDTO getDashboard(Long userId, LocalDate date) {
        List<TaskResponseDTO> tasks = taskService.findForDate(userId, date);
        int completedTasks = (int) tasks.stream().filter(TaskResponseDTO::completed).count();

        List<Activity> activities = activityRepository.findByUserIdAndDate(userId, date);
        int burned = activities.stream().mapToInt(a -> a.getCaloriesBurned() != null ? a.getCaloriesBurned() : 0).sum();

        DailyNutritionDTO nutrition = nutritionGoalService.getDaily(userId, date);
        BodyMetricsDTO bodyMetrics = bodyMetricsService.getMetrics(userId);

        LocalDate monday = date.with(DayOfWeek.MONDAY);
        List<WorkoutResponseDTO> todayWorkouts = workoutRepository.findByUserIdAndDate(userId, date).stream()
                .map(WorkoutResponseDTO::from)
                .toList();
        int workoutsThisWeek = (int) workoutRepository.countByUserIdAndDateBetween(userId, monday, monday.plusDays(6));

        return new DashboardDTO(
                date,
                userService.findById(userId),
                progressSummary(progressService.findByUserId(userId)),
                streakService.getStreak(userId),
                new DashboardDTO.TasksSummary(tasks.size(), completedTasks, tasks),
                nutrition,
                caloriesBalance(nutrition, bodyMetrics, burned),
                waterService.getDay(userId, date),
                new DashboardDTO.ActivitiesSummary(
                        activities.size(),
                        activities.stream().mapToInt(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 0).sum(),
                        burned),
                new DashboardDTO.WorkoutsSummary(todayWorkouts, workoutsThisWeek),
                bodyMetrics,
                weightSummary(weightService.history(userId, date.minusDays(WEIGHT_WINDOW_DAYS), date)),
                strengthService.overallRank(userId));
    }

    static DashboardDTO.ProgressSummary progressSummary(ProgressResponseDTO progress) {
        int totalXp = progress.totalXp() != null ? progress.totalXp() : 0;
        int level = progress.level() != null ? progress.level() : 1;
        int xpInLevel = totalXp - (level - 1) * ProgressService.XP_PER_LEVEL;
        return new DashboardDTO.ProgressSummary(
                totalXp,
                level,
                ProgressService.XP_PER_LEVEL - xpInLevel,
                xpInLevel * 100 / ProgressService.XP_PER_LEVEL,
                progress.category());
    }

    // Meta definida pelo usuario; sem meta, usa o gasto diario calculado (TDEE)
    static DashboardDTO.CaloriesBalance caloriesBalance(DailyNutritionDTO nutrition, BodyMetricsDTO metrics, int burned) {
        Integer target = nutrition.goal().dailyCalories();
        String source = "GOAL";
        if (target == null && metrics.tdee() != null) {
            target = metrics.tdee();
            source = "TDEE";
        }
        if (target == null) {
            source = null;
        }
        int consumed = nutrition.consumed().calories();
        return new DashboardDTO.CaloriesBalance(target, source, consumed, burned,
                target != null ? target + burned - consumed : null);
    }

    private DashboardDTO.WeightSummary weightSummary(WeightHistoryDTO history) {
        List<WeightEntryDTO> entries = history.entries();
        LocalDate last = entries.isEmpty() ? null : entries.get(entries.size() - 1).date();
        return new DashboardDTO.WeightSummary(history.currentWeightKg(), history.changeKg(), last);
    }
}
