package com.vhfantes.fantesday.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// Garante que a base complementar de alimentos (resources/seed/extra-foods.json) esta consistente
class ExtraFoodsSeedTest {

    // Colunas numeric(7,2) do banco
    private static final BigDecimal MAX_VALUE = BigDecimal.valueOf(99_999);

    private List<ExtraFoodSeed> foods;

    @BeforeEach
    void setUp() throws IOException {
        try (InputStream input = new ClassPathResource("seed/extra-foods.json").getInputStream()) {
            foods = JsonMapper.builder().build().readValue(input, new TypeReference<List<ExtraFoodSeed>>() {
            });
        }
    }

    @Test
    void idsAndNamesAreUnique() {
        assertThat(foods).isNotEmpty();
        assertThat(foods).extracting(ExtraFoodSeed::externalId).doesNotContainNull().doesNotHaveDuplicates()
                .allMatch(id -> id.matches("[a-z0-9]+(-[a-z0-9]+)*"));
        assertThat(foods).extracting(ExtraFoodSeed::name).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void everyFoodHasCompleteNonNegativeValuesAndAServing() {
        for (ExtraFoodSeed food : foods) {
            assertThat(List.of(food.energyKcal(), food.protein(), food.carbohydrate(), food.lipid()))
                    .as(food.externalId())
                    .doesNotContainNull()
                    .allMatch(value -> value.signum() >= 0 && value.compareTo(MAX_VALUE) <= 0);
            assertThat(food.servingGrams()).as(food.externalId()).isNotNull().isPositive();
            assertThat(food.servingDescription()).as(food.externalId()).isNotBlank().hasSizeLessThanOrEqualTo(100);
            if (Boolean.TRUE.equals(food.liquid())) {
                assertThat(food.densityGPerMl()).as(food.externalId()).isNotNull().isBetween(
                        new BigDecimal("0.7"), new BigDecimal("1.5"));
            }
        }
    }

    // As calorias tem que bater com os macros (4 kcal/g de proteina e carboidrato, 9 kcal/g de gordura).
    // Bebidas alcoolicas ficam de fora: o alcool tem 7 kcal/g e nao e um macro da tabela.
    @Test
    void caloriesMatchMacros() {
        for (ExtraFoodSeed food : foods) {
            double fromMacros = 4 * food.protein().doubleValue() + 4 * food.carbohydrate().doubleValue()
                    + 9 * food.lipid().doubleValue();
            double kcal = food.energyKcal().doubleValue();
            if (kcal < 20 && fromMacros < 20) {
                continue; // quase sem calorias (refrigerante zero, creatina...)
            }
            if (Boolean.TRUE.equals(food.liquid()) && fromMacros < kcal * 0.6) {
                continue; // bebida alcoolica
            }
            assertThat(fromMacros).as(food.externalId() + " (" + kcal + " kcal)").isBetween(kcal * 0.8, kcal * 1.2);
        }
    }
}
