package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.DailyNutritionDTO;
import com.vhfantes.fantesday.dto.MealNutritionDTO;
import com.vhfantes.fantesday.dto.NutritionGoalRequestDTO;
import com.vhfantes.fantesday.dto.NutritionGoalResponseDTO;
import com.vhfantes.fantesday.dto.NutritionRemainingDTO;
import com.vhfantes.fantesday.entity.NutritionGoal;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.repository.MealItemRepository;
import com.vhfantes.fantesday.repository.NutritionGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NutritionGoalService {

    private static final int MAX_DAILY_CALORIES = 20_000;
    private static final BigDecimal MAX_MACRO_GRAMS = BigDecimal.valueOf(2_000);

    private final NutritionGoalRepository nutritionGoalRepository;
    private final MealItemRepository mealItemRepository;
    private final UserService userService;

    // Se o usuario ainda nao definiu metas, devolve tudo null
    @Transactional(readOnly = true)
    public NutritionGoalResponseDTO findByUser(Long userId) {
        userService.getUserOrThrow(userId);
        return nutritionGoalRepository.findById(userId)
                .map(NutritionGoalResponseDTO::from)
                .orElse(NutritionGoalResponseDTO.empty(userId));
    }

    // Cria as metas na primeira vez; depois altera so os campos enviados
    @Transactional
    public NutritionGoalResponseDTO update(Long userId, NutritionGoalRequestDTO dto) {
        NutritionGoal goal = nutritionGoalRepository.findById(userId).orElseGet(() -> {
            NutritionGoal created = new NutritionGoal();
            created.setUser(userService.getUserOrThrow(userId));
            return created;
        });

        if (dto.dailyCalories() != null) {
            if (dto.dailyCalories() <= 0 || dto.dailyCalories() > MAX_DAILY_CALORIES) {
                throw new BusinessException("Meta de calorias deve estar entre 1 e " + MAX_DAILY_CALORIES);
            }
            goal.setDailyCalories(dto.dailyCalories());
        }
        if (dto.proteinG() != null) {
            goal.setProteinG(validateMacro(dto.proteinG(), "proteina"));
        }
        if (dto.carbsG() != null) {
            goal.setCarbsG(validateMacro(dto.carbsG(), "carboidrato"));
        }
        if (dto.fatG() != null) {
            goal.setFatG(validateMacro(dto.fatG(), "gordura"));
        }

        // saveAndFlush para o @UpdateTimestamp ja vir preenchido na resposta
        return NutritionGoalResponseDTO.from(nutritionGoalRepository.saveAndFlush(goal));
    }

    // Consumo do dia (todas as refeicoes da data) comparado com as metas
    @Transactional(readOnly = true)
    public DailyNutritionDTO getDaily(Long userId, LocalDate date) {
        NutritionGoalResponseDTO goal = findByUser(userId);
        MealItemRepository.DailyTotals totals = mealItemRepository.sumByUserAndDate(userId, date);

        MealNutritionDTO consumed = new MealNutritionDTO(
                totals.getCalories().intValue(), totals.getProtein(), totals.getCarbs(), totals.getFat());

        NutritionRemainingDTO remaining = new NutritionRemainingDTO(
                goal.dailyCalories() != null ? goal.dailyCalories() - consumed.calories() : null,
                subtract(goal.proteinG(), consumed.protein()),
                subtract(goal.carbsG(), consumed.carbs()),
                subtract(goal.fatG(), consumed.fat()));

        Integer caloriesPercent = goal.dailyCalories() != null
                ? (int) Math.round(consumed.calories() * 100.0 / goal.dailyCalories())
                : null;

        return new DailyNutritionDTO(date, consumed, goal, remaining, caloriesPercent);
    }

    private BigDecimal validateMacro(BigDecimal value, String field) {
        if (value.signum() < 0 || value.compareTo(MAX_MACRO_GRAMS) > 0) {
            throw new BusinessException("Meta de " + field + " deve estar entre 0 e " + MAX_MACRO_GRAMS + " g");
        }
        return value;
    }

    private BigDecimal subtract(BigDecimal goal, BigDecimal consumed) {
        return goal != null ? goal.subtract(consumed) : null;
    }
}
