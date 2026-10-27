package com.vhfantes.fantesday.food;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.exception.BusinessException;

// Unidades aceitas nos itens de refeicao: gramas ("g") e mililitros ("ml").
// Os valores nutricionais sao por 100 g; em ml, a quantidade vira gramas pela densidade do alimento.
public final class FoodUnits {

    public static final String GRAMS = "g";
    public static final String MILLILITERS = "ml";
    public static final BigDecimal DEFAULT_DENSITY = BigDecimal.ONE;

    private FoodUnits() {
    }

    // Aceita variacoes ("G", "gramas", "mL", "mililitros"); sem unidade, usa a padrao do alimento
    public static String normalize(String unit, Food food) {
        if (unit == null || unit.isBlank()) {
            return defaultUnit(food);
        }
        return switch (unit.trim().toLowerCase()) {
            case "g", "gr", "grama", "gramas" -> GRAMS;
            case "ml", "mililitro", "mililitros" -> MILLILITERS;
            default -> throw new BusinessException("Unidade invalida: " + unit + " (use g ou ml)");
        };
    }

    // Para dados ja gravados: unidade desconhecida (registros antigos) e tratada como gramas, sem erro
    public static String normalizeLenient(String unit, Food food) {
        try {
            return normalize(unit, food);
        } catch (BusinessException e) {
            return GRAMS;
        }
    }

    public static String defaultUnit(Food food) {
        if (food != null && food.getDefaultUnit() != null && !food.getDefaultUnit().isBlank()) {
            return food.getDefaultUnit().trim().toLowerCase().equals(MILLILITERS) ? MILLILITERS : GRAMS;
        }
        return food != null && Boolean.TRUE.equals(food.getLiquid()) ? MILLILITERS : GRAMS;
    }

    // Quantidade em gramas, usada nos calculos nutricionais
    public static BigDecimal toGrams(BigDecimal quantity, String unit, Food food) {
        if (quantity == null) {
            return null;
        }
        if (!MILLILITERS.equals(unit)) {
            return quantity;
        }
        BigDecimal density = food != null && food.getDensityGPerMl() != null ? food.getDensityGPerMl() : DEFAULT_DENSITY;
        return quantity.multiply(density).setScale(2, RoundingMode.HALF_UP);
    }
}
