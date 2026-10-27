package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.TaskRequestDTO;
import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.entity.Task;
import com.vhfantes.fantesday.entity.TaskCompletion;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.TaskCompletionRepository;
import com.vhfantes.fantesday.repository.TaskRepository;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.task.TaskSchedule;
import com.vhfantes.fantesday.task.WeekDays;

import lombok.RequiredArgsConstructor;

// Tarefas avulsas (concluidas uma vez) e recorrentes (concluidas uma vez por dia, nos dias de weekDays)
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Comparator<Task> BY_TIME = Comparator
            .comparing(Task::getTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Task::getId);

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final UserService userService;
    private final ProgressService progressService;
    private final CurrentUser currentUser;

    // XP ganho ao concluir uma tarefa (app.xp.task-completed)
    @Value("${app.xp.task-completed}")
    private int taskCompletedXp;

    // Todas as tarefas do usuario; "completed" se refere a hoje nas recorrentes
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> findByUser(Long userId) {
        return toResponses(taskRepository.findByUserId(userId), LocalDate.now(), false);
    }

    // Tarefas do dia: recorrentes daquele dia da semana, avulsas daquela data e avulsas sem data
    // (estas somem nos dias seguintes depois de concluidas)
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> findForDate(Long userId, LocalDate date) {
        return toResponses(taskRepository.findByUserId(userId), date, true);
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO findById(Long id) {
        Task task = getTaskOrThrow(id);
        return toResponse(task, LocalDate.now(), taskCompletionRepository.findByTaskId(id));
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
        task.setWeekDays(WeekDays.normalize(dto.weekDays()));

        return toResponse(taskRepository.save(task), LocalDate.now(), List.of());
    }

    // Atualiza apenas os campos enviados (os que vierem null sao ignorados).
    // weekDays = "" transforma a tarefa recorrente em avulsa.
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
            task.setWeekDays(WeekDays.normalize(dto.weekDays()));
        }

        return toResponse(task, LocalDate.now(), taskCompletionRepository.findByTaskId(id));
    }

    // Marca/desmarca a tarefa no dia (padrao: hoje). Concluir da XP e desmarcar remove o XP daquela conclusao;
    // repetir a mesma acao nao muda nada.
    @Transactional
    public TaskResponseDTO setCompleted(Long id, boolean completed, LocalDate date) {
        Task task = getTaskOrThrow(id);
        LocalDate day = date != null ? date : LocalDate.now();
        Long userId = task.getUser().getId();
        boolean recurring = TaskSchedule.isRecurring(task);

        if (day.isAfter(LocalDate.now())) {
            throw new BusinessException("Nao da para concluir uma tarefa em uma data futura");
        }
        if (recurring && !TaskSchedule.isScheduledOn(task, day)) {
            throw new BusinessException("A tarefa nao se repete em " + day);
        }

        // Recorrente: conclusao daquele dia. Avulsa: a conclusao existente, em qualquer dia.
        List<TaskCompletion> existing = recurring
                ? taskCompletionRepository.findByTaskIdAndDate(id, day).stream().toList()
                : taskCompletionRepository.findByTaskId(id);

        if (completed && existing.isEmpty()) {
            TaskCompletion completion = new TaskCompletion();
            completion.setTask(task);
            completion.setDate(day);
            completion.setXpEarned(taskCompletedXp);
            taskCompletionRepository.saveAndFlush(completion);
            progressService.addXp(userId, taskCompletedXp);
        } else if (!completed && !existing.isEmpty()) {
            for (TaskCompletion completion : existing) {
                progressService.addXp(userId, -xpOf(completion));
                taskCompletionRepository.delete(completion);
            }
        }

        return toResponse(task, day, taskCompletionRepository.findByTaskId(id));
    }

    // Tarefa avulsa concluida: desconta o XP. Recorrente: o XP dos dias ja concluidos fica (e historico).
    @Transactional
    public void delete(Long id) {
        Task task = getTaskOrThrow(id);
        List<TaskCompletion> completions = taskCompletionRepository.findByTaskId(id);

        if (!TaskSchedule.isRecurring(task)) {
            int xp = completions.stream().mapToInt(this::xpOf).sum();
            if (xp != 0) {
                progressService.addXp(task.getUser().getId(), -xp);
            }
        }

        taskCompletionRepository.deleteAll(completions);
        taskRepository.delete(task);
    }

    private List<TaskResponseDTO> toResponses(List<Task> tasks, LocalDate date, boolean onlyScheduled) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<Long, List<TaskCompletion>> completionsByTask = taskCompletionRepository
                .findByTaskIdIn(tasks.stream().map(Task::getId).toList()).stream()
                .collect(Collectors.groupingBy(completion -> completion.getTask().getId()));

        return tasks.stream()
                .filter(task -> !onlyScheduled || isVisibleOn(task, date, completionsByTask.getOrDefault(task.getId(), List.of())))
                .sorted(BY_TIME)
                .map(task -> toResponse(task, date, completionsByTask.getOrDefault(task.getId(), List.of())))
                .toList();
    }

    private boolean isVisibleOn(Task task, LocalDate date, List<TaskCompletion> completions) {
        if (!TaskSchedule.isScheduledOn(task, date)) {
            return false;
        }
        // Avulsa sem data concluida: aparece so no dia em que foi concluida
        if (!TaskSchedule.isRecurring(task) && task.getDate() == null && !completions.isEmpty()) {
            return completions.stream().anyMatch(completion -> completion.getDate().equals(date));
        }
        return true;
    }

    private TaskResponseDTO toResponse(Task task, LocalDate date, List<TaskCompletion> completions) {
        TaskCompletion completion = TaskSchedule.isRecurring(task)
                ? completions.stream().filter(c -> c.getDate().equals(date)).findFirst().orElse(null)
                : completions.stream().findFirst().orElse(null);
        return TaskResponseDTO.from(task, date, completion);
    }

    private int xpOf(TaskCompletion completion) {
        return completion.getXpEarned() != null ? completion.getXpEarned() : 0;
    }

    // So o dono da tarefa pode acessa-la
    private Task getTaskOrThrow(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada: " + id));
        currentUser.checkAccess(task.getUser().getId());
        return task;
    }
}
