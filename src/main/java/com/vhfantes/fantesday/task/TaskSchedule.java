package com.vhfantes.fantesday.task;

import java.time.LocalDate;

import com.vhfantes.fantesday.entity.Task;

// Regras de quando uma tarefa aparece em um dia
public final class TaskSchedule {

    private TaskSchedule() {
    }

    public static boolean isRecurring(Task task) {
        return !WeekDays.parse(task.getWeekDays()).isEmpty();
    }

    // Recorrente: nos dias da semana configurados, a partir de task.date (se houver).
    // Avulsa com data: so naquele dia. Avulsa sem data: todo dia (ate ser concluida; quem filtra e o service).
    public static boolean isScheduledOn(Task task, LocalDate date) {
        if (isRecurring(task)) {
            boolean started = task.getDate() == null || !date.isBefore(task.getDate());
            return started && WeekDays.parse(task.getWeekDays()).contains(date.getDayOfWeek());
        }
        return task.getDate() == null || task.getDate().equals(date);
    }
}
