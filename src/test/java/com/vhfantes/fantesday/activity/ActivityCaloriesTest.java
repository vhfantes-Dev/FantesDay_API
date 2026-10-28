package com.vhfantes.fantesday.activity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ActivityCaloriesTest {

    @Test
    void estimatesCaloriesByMetWeightAndHours() {
        // Volei (MET 4.0), 70 kg, 2 horas: 4 x 70 x 2 = 560 kcal
        assertThat(ActivityCalories.estimate(new BigDecimal("4.0"), BigDecimal.valueOf(70), 120)).isEqualTo(560);
        // Corrida (MET 9.8), 80 kg, 30 min: 9.8 x 80 x 0.5 = 392 kcal
        assertThat(ActivityCalories.estimate(new BigDecimal("9.8"), BigDecimal.valueOf(80), 30)).isEqualTo(392);
    }

    @Test
    void usesDefaultWeightWhenUserHasNoWeight() {
        assertThat(ActivityCalories.estimate(new BigDecimal("4.0"), null, 60)).isEqualTo(280);
    }

    @Test
    void noEstimateWithoutMetOrDuration() {
        assertThat(ActivityCalories.estimate(null, BigDecimal.valueOf(70), 60)).isNull();
        assertThat(ActivityCalories.estimate(new BigDecimal("4.0"), BigDecimal.valueOf(70), null)).isNull();
        assertThat(ActivityCalories.estimate(new BigDecimal("4.0"), BigDecimal.valueOf(70), 0)).isNull();
    }

    @Test
    void findsActivityInFreeText() {
        assertThat(KnownActivity.find("joguei vôlei por 2 horas")).contains(KnownActivity.VOLEI);
        assertThat(KnownActivity.find("Corri 5 km")).contains(KnownActivity.CORRIDA);
        assertThat(KnownActivity.find("fiz musculação")).contains(KnownActivity.MUSCULACAO);
        assertThat(KnownActivity.find("VOLEI_DE_PRAIA")).contains(KnownActivity.VOLEI_DE_PRAIA);
        assertThat(KnownActivity.find("dormi a tarde toda")).isEmpty();
    }

    @Test
    void longestMatchWins() {
        assertThat(KnownActivity.find("joguei volei de praia")).contains(KnownActivity.VOLEI_DE_PRAIA);
        assertThat(KnownActivity.find("andei de bicicleta")).contains(KnownActivity.CICLISMO);
        assertThat(KnownActivity.find("tênis de mesa com amigos")).contains(KnownActivity.TENIS_DE_MESA);
    }

    @Test
    void doesNotMatchPartOfAWord() {
        // "corri" nao pode bater dentro de "corrimao"
        assertThat(KnownActivity.find("segurei no corrimao")).isEmpty();
    }

    @Test
    void parsesDurationsInManyFormats() {
        assertThat(ActivityTextParser.parseDurationMinutes("joguei volei por 2 horas")).isEqualTo(120);
        assertThat(ActivityTextParser.parseDurationMinutes("corri 45 min")).isEqualTo(45);
        assertThat(ActivityTextParser.parseDurationMinutes("pedalei 1h30")).isEqualTo(90);
        assertThat(ActivityTextParser.parseDurationMinutes("pedalei 1h30min")).isEqualTo(90);
        assertThat(ActivityTextParser.parseDurationMinutes("nadei 1 hora e 15 minutos")).isEqualTo(75);
        assertThat(ActivityTextParser.parseDurationMinutes("uma hora e meia de futebol")).isEqualTo(90);
        assertThat(ActivityTextParser.parseDurationMinutes("1,5 hora de trilha")).isEqualTo(90);
        assertThat(ActivityTextParser.parseDurationMinutes("meia hora de yoga")).isEqualTo(30);
        assertThat(ActivityTextParser.parseDurationMinutes("duas horas de tenis")).isEqualTo(120);
        assertThat(ActivityTextParser.parseDurationMinutes("quarenta e cinco minutos de pilates")).isEqualTo(45);
        assertThat(ActivityTextParser.parseDurationMinutes("caminhei 3 km em 40 minutos")).isEqualTo(40);
    }

    @Test
    void returnsNullWithoutDuration() {
        assertThat(ActivityTextParser.parseDurationMinutes("joguei volei")).isNull();
        assertThat(ActivityTextParser.parseDurationMinutes("corri 5 km")).isNull();
        assertThat(ActivityTextParser.parseDurationMinutes(null)).isNull();
    }
}
