package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.DashboardDTO;
import com.vhfantes.fantesday.dto.ProgressResponseDTO;
import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.entity.Activity;
import com.vhfantes.fantesday.repository.ActivityRepository;

import lombok.RequiredArgsConstructor;

// Junta os dados da tela inicial (perfil, XP, streak, tarefas, alimentacao, agua, atividades e rank)
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final ProgressService progressService;
    private final StreakService streakService;
    private final TaskService taskService;
    private final NutritionGoalService nutritionGoalService;
    private final WaterService waterService;
    private final StrengthService strengthService;
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public DashboardDTO getDashboard(Long userId, LocalDate date) {
        List<TaskResponseDTO> tasks = taskService.findForDate(userId, date);
        int completedTasks = (int) tasks.stream().filter(TaskResponseDTO::completed).count();

        List<Activity> activities = activityRepository.findByUserIdAndDate(userId, date);

        return new DashboardDTO(
                date,
                userService.findById(userId),
                progressSummary(progressService.findByUserId(userId)),
                streakService.getStreak(userId),
                new DashboardDTO.TasksSummary(tasks.size(), completedTasks, tasks),
                nutritionGoalService.getDaily(userId, date),
                waterService.getDay(userId, date),
                new DashboardDTO.ActivitiesSummary(
                        activities.size(),
                        activities.stream().mapToInt(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 0).sum(),
                        activities.stream().mapToInt(a -> a.getCaloriesBurned() != null ? a.getCaloriesBurned() : 0).sum()),
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
}
