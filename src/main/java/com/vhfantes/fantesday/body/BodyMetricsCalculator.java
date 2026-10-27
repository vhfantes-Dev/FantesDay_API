package com.vhfantes.fantesday.body;

import java.time.LocalDate;
import java.time.Period;

import com.vhfantes.fantesday.entity.Sex;

// Calculos corporais puros (sem banco), para facilitar os testes
public final class BodyMetricsCalculator {

    // Deficit/superavit usados nas sugestoes de meta
    public static final int LOSE_DEFICIT_KCAL = 500;
    public static final int GAIN_SURPLUS_KCAL = 300;

    // Pisos de seguranca para a sugestao de emagrecimento
    public static final int MIN_KCAL_MALE = 1500;
    public static final int MIN_KCAL_FEMALE = 1200;

    // Proteina sugerida por kg de peso corporal
    public static final double PROTEIN_G_PER_KG = 1.6;

    private BodyMetricsCalculator() {
    }

    public static int age(LocalDate birthDate, LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }

    // TMB pela equacao de Mifflin-St Jeor: 10 x peso + 6,25 x altura - 5 x idade + (5 homens | -161 mulheres)
    public static int bmr(double weightKg, double heightCm, int age, Sex sex) {
        double base = 10 * weightKg + 6.25 * heightCm - 5 * age;
        return (int) Math.round(base + (sex == Sex.FEMALE ? -161 : 5));
    }

    // Gasto calorico diario total (TDEE) = TMB x fator de atividade
    public static int tdee(int bmr, double activityFactor) {
        return (int) Math.round(bmr * activityFactor);
    }

    // IMC = peso / altura(m)^2, com 1 casa decimal
    public static double bmi(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;
        return Math.round(weightKg / (heightM * heightM) * 10) / 10.0;
    }

    // Faixas da OMS para adultos
    public static String bmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "Abaixo do peso";
        }
        if (bmi < 25) {
            return "Peso normal";
        }
        if (bmi < 30) {
            return "Sobrepeso";
        }
        if (bmi < 35) {
            return "Obesidade grau I";
        }
        if (bmi < 40) {
            return "Obesidade grau II";
        }
        return "Obesidade grau III";
    }

    public static int loseWeightCalories(int tdee, Sex sex) {
        int floor = sex == Sex.FEMALE ? MIN_KCAL_FEMALE : MIN_KCAL_MALE;
        return Math.max(floor, tdee - LOSE_DEFICIT_KCAL);
    }

    public static int gainWeightCalories(int tdee) {
        return tdee + GAIN_SURPLUS_KCAL;
    }

    public static int proteinGrams(double weightKg) {
        return (int) Math.round(weightKg * PROTEIN_G_PER_KG);
    }
}
