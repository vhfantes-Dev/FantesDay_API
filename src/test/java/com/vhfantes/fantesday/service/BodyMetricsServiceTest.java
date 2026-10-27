package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.dto.BodyMetricsDTO;
import com.vhfantes.fantesday.dto.DashboardDTO;
import com.vhfantes.fantesday.dto.DailyNutritionDTO;
import com.vhfantes.fantesday.dto.MealNutritionDTO;
import com.vhfantes.fantesday.dto.NutritionGoalResponseDTO;
import com.vhfantes.fantesday.entity.ActivityLevel;
import com.vhfantes.fantesday.entity.Sex;
import com.vhfantes.fantesday.entity.User;

class BodyMetricsServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 27);

    @Test
    void calculatesEverythingWhenTheProfileIsComplete() {
        User user = user(new BigDecimal("80"), new BigDecimal("180"), LocalDate.of(1996, 1, 1), Sex.MALE, ActivityLevel.MODERATE);

        BodyMetricsDTO metrics = BodyMetricsService.calculate(user, TODAY);

        assertThat(metrics.complete()).isTrue();
        assertThat(metrics.age()).isEqualTo(30);
        assertThat(metrics.bmr()).isEqualTo(1780);
        assertThat(metrics.tdee()).isEqualTo(2759);
        assertThat(metrics.bmi()).isEqualTo(24.7);
        assertThat(metrics.suggestedCalories().lose()).isEqualTo(2259);
        assertThat(metrics.suggestedCalories().maintain()).isEqualTo(2759);
        assertThat(metrics.suggestedCalories().gain()).isEqualTo(3059);
        assertThat(metrics.suggestedProteinG()).isEqualTo(128);
    }

    @Test
    void listsMissingFieldsAndUsesSedentaryByDefault() {
        User user = user(new BigDecimal("70"), new BigDecimal("170"), null, null, null);

        BodyMetricsDTO metrics = BodyMetricsService.calculate(user, TODAY);

        assertThat(metrics.complete()).isFalse();
        assertThat(metrics.missingFields()).containsExactly("birthDate", "sex");
        assertThat(metrics.bmr()).isNull();
        assertThat(metrics.bmi()).isEqualTo(24.2); // IMC so precisa de peso e altura
        assertThat(metrics.activityLevel()).isEqualTo(ActivityLevel.SEDENTARY);
    }

    @Test
    void caloriesBalanceUsesGoalOrTdee() {
        BodyMetricsDTO metrics = BodyMetricsService.calculate(
                user(new BigDecimal("80"), new BigDecimal("180"), LocalDate.of(1996, 1, 1), Sex.MALE, ActivityLevel.MODERATE), TODAY);
        MealNutritionDTO consumed = new MealNutritionDTO(1000, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        DashboardDTO.CaloriesBalance withGoal = DashboardService.caloriesBalance(daily(consumed, 2000), metrics, 300);
        assertThat(withGoal.target()).isEqualTo(2000);
        assertThat(withGoal.targetSource()).isEqualTo("GOAL");
        assertThat(withGoal.remaining()).isEqualTo(1300); // 2000 + 300 - 1000

        DashboardDTO.CaloriesBalance withoutGoal = DashboardService.caloriesBalance(daily(consumed, null), metrics, 0);
        assertThat(withoutGoal.target()).isEqualTo(2759);
        assertThat(withoutGoal.targetSource()).isEqualTo("TDEE");
    }

    private DailyNutritionDTO daily(MealNutritionDTO consumed, Integer goalCalories) {
        NutritionGoalResponseDTO goal = new NutritionGoalResponseDTO(1L, goalCalories, null, null, null, null, null);
        return new DailyNutritionDTO(TODAY, consumed, goal, null, null);
    }

    private User user(BigDecimal weight, BigDecimal height, LocalDate birthDate, Sex sex, ActivityLevel level) {
        User user = new User();
        user.setBodyweightKg(weight);
        user.setHeightCm(height);
        user.setBirthDate(birthDate);
        user.setSex(sex);
        user.setActivityLevel(level);
        return user;
    }
}
