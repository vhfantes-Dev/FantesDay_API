package com.vhfantes.fantesday.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.WorkoutExerciseRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutExerciseResponseDTO;
import com.vhfantes.fantesday.dto.WorkoutRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutResponseDTO;
import com.vhfantes.fantesday.service.WorkoutService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    // ---------- Treinos ----------

    @GetMapping("/users/{userId}/workouts")
    public List<WorkoutResponseDTO> findByUser(@PathVariable Long userId) {
        return workoutService.findByUser(userId);
    }

    @PostMapping("/users/{userId}/workouts")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponseDTO create(@PathVariable Long userId, @RequestBody WorkoutRequestDTO dto) {
        return workoutService.create(userId, dto);
    }

    @GetMapping("/workouts/{id}")
    public WorkoutResponseDTO findById(@PathVariable Long id) {
        return workoutService.findById(id);
    }

    @PatchMapping("/workouts/{id}")
    public WorkoutResponseDTO update(@PathVariable Long id, @RequestBody WorkoutRequestDTO dto) {
        return workoutService.update(id, dto);
    }

    @DeleteMapping("/workouts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workoutService.delete(id);
    }

    // ---------- Exercicios do treino ----------

    @GetMapping("/workouts/{workoutId}/exercises")
    public List<WorkoutExerciseResponseDTO> findExercises(@PathVariable Long workoutId) {
        return workoutService.findExercises(workoutId);
    }

    @PostMapping("/workouts/{workoutId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutExerciseResponseDTO addExercise(@PathVariable Long workoutId,
            @RequestBody WorkoutExerciseRequestDTO dto) {
        return workoutService.addExercise(workoutId, dto);
    }

    @PatchMapping("/workout-exercises/{itemId}")
    public WorkoutExerciseResponseDTO updateExercise(@PathVariable Long itemId,
            @RequestBody WorkoutExerciseRequestDTO dto) {
        return workoutService.updateExercise(itemId, dto);
    }

    @DeleteMapping("/workout-exercises/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeExercise(@PathVariable Long itemId) {
        workoutService.removeExercise(itemId);
    }
}
