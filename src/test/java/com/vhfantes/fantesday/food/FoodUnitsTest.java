package com.vhfantes.fantesday.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.exception.BusinessException;

class FoodUnitsTest {

    @Test
    void normalizesUnitsAndUsesTheFoodDefault() {
        Food milk = liquid("ml", "1.03");
        Food rice = new Food();

        assertThat(FoodUnits.normalize("ML", rice)).isEqualTo("ml");
        assertThat(FoodUnits.normalize("gramas", milk)).isEqualTo("g");
        assertThat(FoodUnits.normalize(null, milk)).isEqualTo("ml");
        assertThat(FoodUnits.normalize(null, rice)).isEqualTo("g");
        assertThatThrownBy(() -> FoodUnits.normalize("xicara", rice)).isInstanceOf(BusinessException.class);
        assertThat(FoodUnits.normalizeLenient("unidade", rice)).isEqualTo("g");
    }

    @Test
    void convertsMillilitersToGramsWithDensity() {
        // 200 ml de leite (1,03 g/ml) = 206 g
        assertThat(FoodUnits.toGrams(new BigDecimal("200"), "ml", liquid("ml", "1.03"))).isEqualByComparingTo("206");
        // 15 ml de azeite (0,91 g/ml) = 13,65 g
        assertThat(FoodUnits.toGrams(new BigDecimal("15"), "ml", liquid("ml", "0.91"))).isEqualByComparingTo("13.65");
        // sem densidade: 1 g/ml
        assertThat(FoodUnits.toGrams(new BigDecimal("300"), "ml", new Food())).isEqualByComparingTo("300");
        // gramas nao mudam
        assertThat(FoodUnits.toGrams(new BigDecimal("150"), "g", liquid("ml", "1.03"))).isEqualByComparingTo("150");
    }

    private Food liquid(String unit, String density) {
        Food food = new Food();
        food.setLiquid(true);
        food.setDefaultUnit(unit);
        food.setDensityGPerMl(new BigDecimal(density));
        return food;
    }
}
