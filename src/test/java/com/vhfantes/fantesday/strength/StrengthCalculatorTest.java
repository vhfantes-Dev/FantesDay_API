package com.vhfantes.fantesday.strength;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class StrengthCalculatorTest {

    // Supino masculino: iniciante 0.5, novato 0.75, intermediario 1.0, avancado 1.5, elite 2.0
    private static final List<Double> BENCH = List.of(0.50, 0.75, 1.00, 1.50, 2.00);

    @Test
    void estimatesOneRepMaxWithEpley() {
        // 100kg x 5 = 100 x (1 + 5/30) = 116.67
        assertThat(StrengthCalculator.estimateOneRepMax(new BigDecimal("100"), 5)).isEqualByComparingTo("116.67");
        assertThat(StrengthCalculator.estimateOneRepMax(new BigDecimal("80"), 1)).isEqualByComparingTo("80");
    }

    @Test
    void doesNotEstimateInvalidSets() {
        assertThat(StrengthCalculator.estimateOneRepMax(null, 5)).isNull();
        assertThat(StrengthCalculator.estimateOneRepMax(BigDecimal.ZERO, 5)).isNull();
        assertThat(StrengthCalculator.estimateOneRepMax(new BigDecimal("40"), 0)).isNull();
        assertThat(StrengthCalculator.estimateOneRepMax(new BigDecimal("40"), 21)).isNull();
    }

    @Test
    void scoreHitsEachAnchor() {
        assertThat(StrengthCalculator.score(0.50, BENCH)).isEqualTo(100);
        assertThat(StrengthCalculator.score(0.75, BENCH)).isEqualTo(250);
        assertThat(StrengthCalculator.score(1.00, BENCH)).isEqualTo(400);
        assertThat(StrengthCalculator.score(1.50, BENCH)).isEqualTo(600);
        assertThat(StrengthCalculator.score(2.00, BENCH)).isEqualTo(800);
    }

    @Test
    void scoreInterpolatesAndCaps() {
        assertThat(StrengthCalculator.score(1.25, BENCH)).isEqualTo(500);
        assertThat(StrengthCalculator.score(0.25, BENCH)).isEqualTo(50);
        assertThat(StrengthCalculator.score(2.50, BENCH)).isEqualTo(900);
        assertThat(StrengthCalculator.score(3.00, BENCH)).isEqualTo(1000);
        assertThat(StrengthCalculator.score(10.0, BENCH)).isEqualTo(1000);
        assertThat(StrengthCalculator.score(0, BENCH)).isZero();
    }

    @Test
    void aggregateWeightsHigherScores() {
        assertThat(StrengthCalculator.aggregate(List.of(400))).isEqualTo(400);
        // (600^2 + 200^2) / (600 + 200) = 500, acima da media simples (400)
        assertThat(StrengthCalculator.aggregate(List.of(600, 200))).isEqualTo(500);
        assertThat(StrengthCalculator.aggregate(List.of())).isZero();
    }

    @Test
    void mapsScoreToRankAndLp() {
        assertThat(StrengthRank.fromScore(0)).isEqualTo(StrengthRank.BRONZE);
        assertThat(StrengthRank.fromScore(99)).isEqualTo(StrengthRank.BRONZE);
        assertThat(StrengthRank.fromScore(100)).isEqualTo(StrengthRank.SILVER);
        assertThat(StrengthRank.fromScore(345)).isEqualTo(StrengthRank.PLATINUM);
        assertThat(StrengthRank.fromScore(650)).isEqualTo(StrengthRank.GRANDMASTER);
        assertThat(StrengthRank.fromScore(799)).isEqualTo(StrengthRank.CHALLENGER);
        assertThat(StrengthRank.lp(345)).isEqualTo(45);
        assertThat(StrengthRank.fromScore(950)).isEqualTo(StrengthRank.OLYMPIAN);
        assertThat(StrengthRank.lp(950)).isEqualTo(150);
        assertThat(StrengthRank.OLYMPIAN.next()).isNull();
        assertThat(StrengthRank.DIAMOND.next()).isEqualTo(StrengthRank.MASTER);
    }
}
