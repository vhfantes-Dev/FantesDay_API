package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MealServiceCalculationTest {

    @Test
    void calculatesProportionalValue() {
        // 150g de arroz com 128.26 kcal/100g = 192.39 kcal
        assertThat(MealService.calculatePer100g(new BigDecimal("128.26"), new BigDecimal("150"), 0))
                .isEqualByComparingTo("192");
        assertThat(MealService.calculatePer100g(new BigDecimal("2.52"), new BigDecimal("150"), 2))
                .isEqualByComparingTo("3.78");
    }

    @Test
    void roundsHalfUp() {
        // 50g x 3.01 / 100 = 1.505 -> 1.51
        assertThat(MealService.calculatePer100g(new BigDecimal("3.01"), new BigDecimal("50"), 2))
                .isEqualByComparingTo("1.51");
    }

    @Test
    void returnsNullWhenValueIsMissing() {
        assertThat(MealService.calculatePer100g(null, new BigDecimal("100"), 2)).isNull();
    }
}
