package com.vhfantes.fantesday.entity;

// Nivel de atividade do dia a dia: multiplica a TMB para estimar o gasto calorico diario (TDEE)
public enum ActivityLevel {
    SEDENTARY(1.2),      // pouco ou nenhum exercicio
    LIGHT(1.375),        // exercicio leve 1 a 3 dias por semana
    MODERATE(1.55),      // exercicio moderado 3 a 5 dias por semana
    ACTIVE(1.725),       // exercicio intenso 6 a 7 dias por semana
    VERY_ACTIVE(1.9);    // exercicio intenso diario ou trabalho fisico

    private final double factor;

    ActivityLevel(double factor) {
        this.factor = factor;
    }

    public double factor() {
        return factor;
    }
}
