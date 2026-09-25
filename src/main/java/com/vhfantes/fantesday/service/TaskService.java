package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.TaskRequestDTO;
import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.entity.Task;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> findByUser(Long userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(TaskResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> findByUserAndDate(Long userId, LocalDate date) {
        return taskRepository.findByUserIdAndDate(userId, date).stream()
                .map(TaskResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO findById(Long id) {
        return TaskResponseDTO.from(getTaskOrThrow(id));
    }

    @Transactional
    public TaskResponseDTO create(Long userId, TaskRequestDTO dto) {
        if (dto.title() == null || dto.title().isBlank()) {
            throw new BusinessException("Titulo e obrigatorio");
        }

        Task task = new Task();
        task.setUser(userService.getUserOrThrow(userId));
        task.setTitle(dto.title().trim());
        task.setType(dto.type());
        task.setDate(dto.date());
        task.setTime(dto.time());
        task.setWeekDays(dto.weekDays());

        return TaskResponseDTO.from(taskRepository.save(task));
    }

    // Atualiza apenas os campos enviados (os que vierem null sao ignorados)
    @Transactional
    public TaskResponseDTO update(Long id, TaskRequestDTO dto) {
        Task task = getTaskOrThrow(id);

        if (dto.title() != null && !dto.title().isBlank()) {
            task.setTitle(dto.title().trim());
        }
        if (dto.type() != null) {
            task.setType(dto.type());
        }
        if (dto.date() != null) {
            task.setDate(dto.date());
        }
        if (dto.time() != null) {
            task.setTime(dto.time());
        }
        if (dto.weekDays() != null) {
            task.setWeekDays(dto.weekDays());
        }

        return TaskResponseDTO.from(task);
    }

    // Marca/desmarca a tarefa como concluida
    @Transactional
    public TaskResponseDTO setCompleted(Long id, boolean completed) {
        Task task = getTaskOrThrow(id);
        task.setCompleted(completed);
        task.setCompletedAt(completed ? LocalDateTime.now() : null);
        return TaskResponseDTO.from(task);
    }

    @Transactional
    public void delete(Long id) {
        taskRepository.delete(getTaskOrThrow(id));
    }

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada: " + id));
    }
}
