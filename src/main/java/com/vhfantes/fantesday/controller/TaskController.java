package com.vhfantes.fantesday.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.TaskRequestDTO;
import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ?date=yyyy-MM-dd filtra pelo dia (opcional)
    @GetMapping("/users/{userId}/tasks")
    public List<TaskResponseDTO> findByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return date != null
                ? taskService.findByUserAndDate(userId, date)
                : taskService.findByUser(userId);
    }

    @PostMapping("/users/{userId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDTO create(@PathVariable Long userId, @RequestBody TaskRequestDTO dto) {
        return taskService.create(userId, dto);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponseDTO findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PatchMapping("/tasks/{id}")
    public TaskResponseDTO update(@PathVariable Long id, @RequestBody TaskRequestDTO dto) {
        return taskService.update(id, dto);
    }

    @PatchMapping("/tasks/{id}/complete")
    public TaskResponseDTO complete(@PathVariable Long id) {
        return taskService.setCompleted(id, true);
    }

    @PatchMapping("/tasks/{id}/uncomplete")
    public TaskResponseDTO uncomplete(@PathVariable Long id) {
        return taskService.setCompleted(id, false);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
