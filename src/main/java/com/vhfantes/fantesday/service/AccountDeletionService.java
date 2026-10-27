package com.vhfantes.fantesday.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.repository.ActivityRepository;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.repository.FoodRepository;
import com.vhfantes.fantesday.repository.MealItemRepository;
import com.vhfantes.fantesday.repository.MealRepository;
import com.vhfantes.fantesday.repository.NutritionGoalRepository;
import com.vhfantes.fantesday.repository.PasswordResetCodeRepository;
import com.vhfantes.fantesday.repository.ProgressRepository;
import com.vhfantes.fantesday.repository.RefreshTokenRepository;
import com.vhfantes.fantesday.repository.RoutineExerciseRepository;
import com.vhfantes.fantesday.repository.TaskCompletionRepository;
import com.vhfantes.fantesday.repository.TaskRepository;
import com.vhfantes.fantesday.repository.UserPhotoRepository;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.repository.WaterEntryRepository;
import com.vhfantes.fantesday.repository.WeightEntryRepository;
import com.vhfantes.fantesday.repository.WorkoutExerciseRepository;
import com.vhfantes.fantesday.repository.WorkoutRepository;
import com.vhfantes.fantesday.repository.WorkoutRoutineRepository;
import com.vhfantes.fantesday.repository.WorkoutSetRepository;

import lombok.RequiredArgsConstructor;

// Exclui a conta e todos os dados do usuario, na ordem das chaves estrangeiras (filhos antes dos pais).
// Alimentos e exercicios que ele cadastrou continuam no catalogo, sem dono, porque outros usuarios podem usa-los.
@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private static final Logger logger = LoggerFactory.getLogger(AccountDeletionService.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final WorkoutRoutineRepository workoutRoutineRepository;
    private final MealItemRepository mealItemRepository;
    private final MealRepository mealRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final TaskRepository taskRepository;
    private final ActivityRepository activityRepository;
    private final WaterEntryRepository waterEntryRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final ProgressRepository progressRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userService.getUserOrThrow(userId);

        workoutSetRepository.deleteByUserId(userId);
        workoutExerciseRepository.deleteByUserId(userId);
        workoutRepository.deleteByUserId(userId);

        routineExerciseRepository.deleteByUserId(userId);
        workoutRoutineRepository.deleteByUserId(userId);

        mealItemRepository.deleteByUserId(userId);
        mealRepository.deleteByUserId(userId);

        taskCompletionRepository.deleteByUserId(userId);
        taskRepository.deleteByUserId(userId);

        activityRepository.deleteByUserId(userId);
        waterEntryRepository.deleteByUserId(userId);
        weightEntryRepository.deleteByUserId(userId);
        refreshTokenRepository.deleteByUserId(userId);
        passwordResetCodeRepository.deleteByUserId(userId);

        foodRepository.clearCreatedBy(userId);
        exerciseRepository.clearCreatedBy(userId);

        nutritionGoalRepository.deleteById(userId);
        progressRepository.deleteById(userId);
        userPhotoRepository.deleteById(userId);

        userRepository.delete(user);
        logger.info("Conta {} excluida com todos os dados", userId);
    }
}
