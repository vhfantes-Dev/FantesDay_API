package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.body.BodyMetricsCalculator;
import com.vhfantes.fantesday.body.WeightGoal;
import com.vhfantes.fantesday.dto.BodyMetricsDTO;
import com.vhfantes.fantesday.dto.NutritionGoalRequestDTO;
import com.vhfantes.fantesday.dto.NutritionGoalResponseDTO;
import com.vhfantes.fantesday.entity.ActivityLevel;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;

import lombok.RequiredArgsConstructor;

// TMB, gasto diario, IMC e sugestao de meta de calorias a partir do perfil do usuario
@Service
@RequiredArgsConstructor
public class BodyMetricsService {

    private final UserService userService;
    private final NutritionGoalService nutritionGoalService;

    @Transactional(readOnly = true)
    public BodyMetricsDTO getMetrics(Long userId) {
        return calculate(userService.getUserOrThrow(userId), LocalDate.now());
    }

    // Usa a sugestao do objetivo escolhido como meta de calorias (e de proteina)
    @Transactional
    public NutritionGoalResponseDTO applySuggestion(Long userId, WeightGoal goal) {
        if (goal == null) {
            throw new BusinessException("Informe o objetivo: LOSE, MAINTAIN ou GAIN");
        }
        BodyMetricsDTO metrics = getMetrics(userId);
        if (!metrics.complete()) {
            throw new BusinessException("Complete o perfil para calcular a meta: " + String.join(", ", metrics.missingFields()));
        }

        int calories = switch (goal) {
            case LOSE -> metrics.suggestedCalories().lose();
            case MAINTAIN -> metrics.suggestedCalories().maintain();
            case GAIN -> metrics.suggestedCalories().gain();
        };
        return nutritionGoalService.update(userId, new NutritionGoalRequestDTO(calories,
                BigDecimal.valueOf(metrics.suggestedProteinG()), null, null, null));
    }

    static BodyMetricsDTO calculate(User user, LocalDate today) {
        List<String> missing = new ArrayList<>();
        if (user.getBodyweightKg() == null) {
            missing.add("bodyweightKg");
        }
        if (user.getHeightCm() == null) {
            missing.add("heightCm");
        }
        if (user.getBirthDate() == null) {
            missing.add("birthDate");
        }
        if (user.getSex() == null) {
            missing.add("sex");
        }

        // Sem nivel de atividade, o gasto diario usa "sedentario"
        ActivityLevel level = user.getActivityLevel() != null ? user.getActivityLevel() : ActivityLevel.SEDENTARY;
        Integer age = user.getBirthDate() != null ? BodyMetricsCalculator.age(user.getBirthDate(), today) : null;

        Double bmi = null;
        String bmiCategory = null;
        if (user.getBodyweightKg() != null && user.getHeightCm() != null) {
            bmi = BodyMetricsCalculator.bmi(user.getBodyweightKg().doubleValue(), user.getHeightCm().doubleValue());
            bmiCategory = BodyMetricsCalculator.bmiCategory(bmi);
        }

        Integer bmr = null;
        Integer tdee = null;
        BodyMetricsDTO.CalorieSuggestions suggestions = null;
        Integer protein = null;
        if (missing.isEmpty()) {
            double weight = user.getBodyweightKg().doubleValue();
            bmr = BodyMetricsCalculator.bmr(weight, user.getHeightCm().doubleValue(), age, user.getSex());
            tdee = BodyMetricsCalculator.tdee(bmr, level.factor());
            suggestions = new BodyMetricsDTO.CalorieSuggestions(
                    BodyMetricsCalculator.loseWeightCalories(tdee, user.getSex()),
                    tdee,
                    BodyMetricsCalculator.gainWeightCalories(tdee));
            protein = BodyMetricsCalculator.proteinGrams(weight);
        }

        return new BodyMetricsDTO(user.getHeightCm(), user.getBodyweightKg(), age, user.getSex(), level, level.factor(),
                bmi, bmiCategory, bmr, tdee, suggestions, protein, missing.isEmpty(), missing);
    }
}
