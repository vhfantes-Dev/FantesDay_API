package com.vhfantes.fantesday.strength;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

// Calculos puros do rank de forca (sem banco), para facilitar os testes
public final class StrengthCalculator {

    // Series com mais repeticoes que isso nao estimam 1RM (a formula perde precisao)
    public static final int MAX_REPS_FOR_ESTIMATE = 20;

    // Nota correspondente a cada nivel dos padroes: iniciante, novato, intermediario, avancado, elite
    private static final int[] ANCHOR_SCORES = { 100, 250, 400, 600, 800 };
    public static final int MAX_SCORE = 1000;

    // Acima do elite, a nota chega a 1000 quando a razao atinge 1.5x o elite
    private static final double ABOVE_ELITE_FACTOR = 0.5;

    private StrengthCalculator() {
    }

    // 1RM estimado pela formula de Epley: carga x (1 + reps / 30). Com 1 repeticao, e a propria carga.
    public static BigDecimal estimateOneRepMax(BigDecimal load, int reps) {
        if (load == null || load.signum() <= 0 || reps <= 0 || reps > MAX_REPS_FOR_ESTIMATE) {
            return null;
        }
        if (reps == 1) {
            return load.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(reps).divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP));
        return load.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    // Converte a razao (1RM / peso corporal) numa nota de 1 a 1000, interpolando entre os 5 niveis do padrao
    public static int score(double ratio, List<Double> anchors) {
        if (ratio <= 0) {
            return 0;
        }

        double first = anchors.get(0);
        if (ratio <= first) {
            return clamp(ANCHOR_SCORES[0] * ratio / first);
        }

        for (int i = 1; i < anchors.size(); i++) {
            double low = anchors.get(i - 1);
            double high = anchors.get(i);
            if (ratio <= high) {
                double progress = (ratio - low) / (high - low);
                return clamp(ANCHOR_SCORES[i - 1] + progress * (ANCHOR_SCORES[i] - ANCHOR_SCORES[i - 1]));
            }
        }

        double elite = anchors.get(anchors.size() - 1);
        double progress = (ratio - elite) / (elite * ABOVE_ELITE_FACTOR);
        return clamp(ANCHOR_SCORES[ANCHOR_SCORES.length - 1] + progress * (MAX_SCORE - ANCHOR_SCORES[ANCHOR_SCORES.length - 1]));
    }

    // Junta varias notas numa so, dando mais peso as notas mais altas (media ponderada pela propria nota)
    public static int aggregate(Collection<Integer> scores) {
        long sum = 0;
        long sumOfSquares = 0;
        for (int score : scores) {
            sum += score;
            sumOfSquares += (long) score * score;
        }
        return sum == 0 ? 0 : (int) Math.round((double) sumOfSquares / sum);
    }

    private static int clamp(double value) {
        return (int) Math.max(1, Math.min(MAX_SCORE, Math.round(value)));
    }
}
