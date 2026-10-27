package com.vhfantes.fantesday.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.entity.Task;
import com.vhfantes.fantesday.exception.BusinessException;

class TaskScheduleTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 26);

    @Test
    void parsesPortugueseAndEnglishDays() {
        assertThat(WeekDays.parse("seg, qua,SEX")).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        assertThat(WeekDays.parse("sáb dom")).containsExactly(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        assertThat(WeekDays.parse("MON,TUE")).containsExactly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        assertThat(WeekDays.parse(null)).isEmpty();
        assertThat(WeekDays.parse("  ")).isEmpty();
    }

    @Test
    void normalizesToPortugueseInWeekOrder() {
        assertThat(WeekDays.normalize("sex,seg,QUA,seg")).isEqualTo("SEG,QUA,SEX");
        assertThat(WeekDays.normalize("")).isNull();
    }

    @Test
    void rejectsInvalidDay() {
        assertThatThrownBy(() -> WeekDays.parse("SEG,XYZ")).isInstanceOf(BusinessException.class);
    }

    @Test
    void recurringTaskRunsOnConfiguredWeekdaysAfterStart() {
        Task task = task(MONDAY.plusDays(7), "SEG,QUA");

        assertThat(TaskSchedule.isRecurring(task)).isTrue();
        assertThat(TaskSchedule.isScheduledOn(task, MONDAY)).isFalse(); // antes do inicio
        assertThat(TaskSchedule.isScheduledOn(task, MONDAY.plusDays(7))).isTrue();
        assertThat(TaskSchedule.isScheduledOn(task, MONDAY.plusDays(9))).isTrue(); // quarta
        assertThat(TaskSchedule.isScheduledOn(task, MONDAY.plusDays(8))).isFalse(); // terca
    }

    @Test
    void oneOffTaskRunsOnItsDateOrEveryDayWithoutDate() {
        assertThat(TaskSchedule.isScheduledOn(task(MONDAY, null), MONDAY)).isTrue();
        assertThat(TaskSchedule.isScheduledOn(task(MONDAY, null), MONDAY.plusDays(1))).isFalse();
        assertThat(TaskSchedule.isScheduledOn(task(null, null), MONDAY.plusDays(30))).isTrue();
    }

    private Task task(LocalDate date, String weekDays) {
        Task task = new Task();
        task.setDate(date);
        task.setWeekDays(weekDays);
        return task;
    }
}
