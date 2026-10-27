package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.vhfantes.fantesday.entity.Task;

public record TaskResponseDTO(
        Long id,
        Long userId,
        String title,
        String type,
        LocalDate date,
        LocalTime time,
        String weekDays,
        Boolean completed,
        LocalDateTime completedAt) {

    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getUser().getId(),
                task.getTitle(),
                task.getType(),
                task.getDate(),
                task.getTime(),
                task.getWeekDays(),
                task.getCompleted(),
                task.getCompletedAt());
    }
}
