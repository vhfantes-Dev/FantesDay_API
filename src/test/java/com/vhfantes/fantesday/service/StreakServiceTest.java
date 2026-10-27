package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.dto.StreakDTO;

class StreakServiceTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 21);

    @Test
    void noWorkoutsMeansNoStreak() {
        StreakDTO streak = StreakService.calculate(List.of(), MONDAY);

        assertThat(streak.currentStreakDays()).isZero();
        assertThat(streak.active()).isFalse();
    }

    @Test
    void restDaysCountWhileStreakIsActive() {
        // treinou segunda e quarta; hoje e sexta (2 dias de descanso desde quarta)
        StreakDTO streak = StreakService.calculate(List.of(MONDAY, MONDAY.plusDays(2)), MONDAY.plusDays(4));

        assertThat(streak.active()).isTrue();
        assertThat(streak.currentStreakDays()).isEqualTo(5);
        assertThat(streak.workoutsInCurrentStreak()).isEqualTo(2);
        assertThat(streak.expiresOn()).isEqualTo(MONDAY.plusDays(5));
    }

    @Test
    void threeRestDaysInARowBreakTheStreak() {
        // segunda, depois so sexta: terca, quarta e quinta sem treino
        List<LocalDate> dates = List.of(MONDAY, MONDAY.plusDays(4));

        StreakDTO streak = StreakService.calculate(dates, MONDAY.plusDays(4));

        assertThat(streak.currentStreakDays()).isEqualTo(1);
        assertThat(streak.workoutsInCurrentStreak()).isEqualTo(1);
        assertThat(streak.longestStreakDays()).isEqualTo(1);
    }

    @Test
    void streakExpiresAfterThreeDaysWithoutWorkout() {
        StreakDTO streak = StreakService.calculate(List.of(MONDAY, MONDAY.plusDays(1)), MONDAY.plusDays(5));

        assertThat(streak.active()).isFalse();
        assertThat(streak.currentStreakDays()).isZero();
        assertThat(streak.longestStreakDays()).isEqualTo(2);
        assertThat(streak.lastWorkoutDate()).isEqualTo(MONDAY.plusDays(1));
    }

    @Test
    void keepsLongestStreakFromThePast() {
        List<LocalDate> dates = List.of(
                MONDAY, MONDAY.plusDays(2), MONDAY.plusDays(5), // 6 dias seguidos contando descanso
                MONDAY.plusDays(20)); // novo streak

        StreakDTO streak = StreakService.calculate(dates, MONDAY.plusDays(20));

        assertThat(streak.longestStreakDays()).isEqualTo(6);
        assertThat(streak.currentStreakDays()).isEqualTo(1);
    }
}
