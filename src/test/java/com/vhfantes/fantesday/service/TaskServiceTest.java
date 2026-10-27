package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.entity.Task;
import com.vhfantes.fantesday.entity.TaskCompletion;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.repository.TaskCompletionRepository;
import com.vhfantes.fantesday.repository.TaskRepository;
import com.vhfantes.fantesday.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final long USER_ID = 7L;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskCompletionRepository taskCompletionRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProgressService progressService;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskService, "taskCompletedXp", 10);
    }

    @Test
    void completingRecurringTaskCreatesCompletionForTheDayAndGivesXp() {
        LocalDate today = LocalDate.now();
        Task task = recurringTaskFor(today);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findByTaskIdAndDate(1L, today)).thenReturn(Optional.empty());

        taskService.setCompleted(1L, true, today);

        verify(taskCompletionRepository).saveAndFlush(any(TaskCompletion.class));
        verify(progressService).addXp(USER_ID, 10);
    }

    @Test
    void completingTwiceDoesNotGiveXpAgain() {
        LocalDate today = LocalDate.now();
        Task task = recurringTaskFor(today);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findByTaskIdAndDate(1L, today)).thenReturn(Optional.of(completion(task, today, 10)));

        taskService.setCompleted(1L, true, today);

        verify(taskCompletionRepository, never()).saveAndFlush(any());
        verify(progressService, never()).addXp(eq(USER_ID), anyInt());
    }

    @Test
    void uncompletingRemovesTheXpOfThatCompletion() {
        LocalDate today = LocalDate.now();
        Task task = recurringTaskFor(today);
        TaskCompletion completion = completion(task, today, 10);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findByTaskIdAndDate(1L, today)).thenReturn(Optional.of(completion));

        taskService.setCompleted(1L, false, today);

        verify(progressService).addXp(USER_ID, -10);
        verify(taskCompletionRepository).delete(completion);
    }

    @Test
    void recurringTaskCannotBeCompletedOnADayItDoesNotRepeat() {
        LocalDate today = LocalDate.now();
        Task task = recurringTaskFor(today.plusDays(1)); // so repete no dia da semana de amanha
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.setCompleted(1L, true, today)).isInstanceOf(BusinessException.class);
    }

    @Test
    void futureDatesAreRejected() {
        Task task = recurringTaskFor(LocalDate.now());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.setCompleted(1L, true, LocalDate.now().plusDays(7)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void dayListShowsRecurringTaskWithThatDaysCompletion() {
        LocalDate today = LocalDate.now();
        Task recurring = recurringTaskFor(today);
        Task otherDay = recurringTaskFor(today.plusDays(1));
        otherDay.setId(2L);
        when(taskRepository.findByUserId(USER_ID)).thenReturn(List.of(recurring, otherDay));
        when(taskCompletionRepository.findByTaskIdIn(any())).thenReturn(List.of(
                completion(recurring, today.minusDays(7), 10), completion(recurring, today, 10)));

        List<TaskResponseDTO> day = taskService.findForDate(USER_ID, today);

        assertThat(day).hasSize(1);
        assertThat(day.get(0).completed()).isTrue();
        assertThat(day.get(0).recurring()).isTrue();
    }

    // Tarefa recorrente que se repete no dia da semana de "day"
    private Task recurringTaskFor(LocalDate day) {
        User user = new User();
        user.setId(USER_ID);

        Task task = new Task();
        task.setId(1L);
        task.setUser(user);
        task.setTitle("Beber agua");
        task.setWeekDays(day.getDayOfWeek().name().substring(0, 3));
        return task;
    }

    private TaskCompletion completion(Task task, LocalDate date, int xp) {
        TaskCompletion completion = new TaskCompletion();
        completion.setTask(task);
        completion.setDate(date);
        completion.setXpEarned(xp);
        return completion;
    }
}
