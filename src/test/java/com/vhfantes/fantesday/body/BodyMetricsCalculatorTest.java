package com.vhfantes.fantesday.body;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.entity.Sex;

class BodyMetricsCalculatorTest {

    @Test
    void bmrFollowsMifflinStJeor() {
        // Homem, 80 kg, 180 cm, 30 anos: 800 + 1125 - 150 + 5 = 1780
        assertThat(BodyMetricsCalculator.bmr(80, 180, 30, Sex.MALE)).isEqualTo(1780);
        // Mulher, 60 kg, 165 cm, 25 anos: 600 + 1031,25 - 125 - 161 = 1345,25
        assertThat(BodyMetricsCalculator.bmr(60, 165, 25, Sex.FEMALE)).isEqualTo(1345);
    }

    @Test
    void tdeeMultipliesByActivityFactor() {
        assertThat(BodyMetricsCalculator.tdee(1780, 1.55)).isEqualTo(2759);
    }

    @Test
    void bmiAndCategory() {
        assertThat(BodyMetricsCalculator.bmi(80, 180)).isEqualTo(24.7);
        assertThat(BodyMetricsCalculator.bmiCategory(24.7)).isEqualTo("Peso normal");
        assertThat(BodyMetricsCalculator.bmiCategory(17)).isEqualTo("Abaixo do peso");
        assertThat(BodyMetricsCalculator.bmiCategory(31)).isEqualTo("Obesidade grau I");
    }

    @Test
    void ageIsCompletedYears() {
        LocalDate today = LocalDate.of(2026, 10, 27);
        assertThat(BodyMetricsCalculator.age(LocalDate.of(1996, 10, 27), today)).isEqualTo(30);
        assertThat(BodyMetricsCalculator.age(LocalDate.of(1996, 10, 28), today)).isEqualTo(29);
    }

    @Test
    void weightLossSuggestionNeverGoesBelowTheFloor() {
        assertThat(BodyMetricsCalculator.loseWeightCalories(2759, Sex.MALE)).isEqualTo(2259);
        assertThat(BodyMetricsCalculator.loseWeightCalories(1600, Sex.FEMALE)).isEqualTo(1200);
        assertThat(BodyMetricsCalculator.loseWeightCalories(1800, Sex.MALE)).isEqualTo(1500);
        assertThat(BodyMetricsCalculator.gainWeightCalories(2759)).isEqualTo(3059);
        assertThat(BodyMetricsCalculator.proteinGrams(80)).isEqualTo(128);
    }
}
