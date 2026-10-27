package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.vhfantes.fantesday.entity.Task;
import com.vhfantes.fantesday.entity.TaskCompletion;
import com.vhfantes.fantesday.task.TaskSchedule;

// completed/completedAt se referem a referenceDate:
// - tarefa recorrente: concluida naquele dia;
// - tarefa avulsa: concluida (em qualquer dia).
public record TaskResponseDTO(
        Long id,
        Long userId,
        String title,
        String type,
        LocalDate date,
        LocalTime time,
        String weekDays,
        boolean recurring,
        LocalDate referenceDate,
        boolean completed,
        LocalDateTime completedAt) {

    public static TaskResponseDTO from(Task task, LocalDate referenceDate, TaskCompletion completion) {
        return new TaskResponseDTO(
                task.getId(),
                task.getUser().getId(),
                task.getTitle(),
                task.getType(),
                task.getDate(),
                task.getTime(),
                task.getWeekDays(),
                TaskSchedule.isRecurring(task),
                referenceDate,
                completion != null,
                completion != null ? completion.getCompletedAt() : null);
    }
}
