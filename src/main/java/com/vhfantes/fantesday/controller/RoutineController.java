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

import com.vhfantes.fantesday.dto.GenerateRoutineRequestDTO;
import com.vhfantes.fantesday.dto.RoutineExerciseRequestDTO;
import com.vhfantes.fantesday.dto.RoutineRequestDTO;
import com.vhfantes.fantesday.dto.RoutineResponseDTO;
import com.vhfantes.fantesday.dto.StartRoutineRequestDTO;
import com.vhfantes.fantesday.dto.StartedWorkoutDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.RoutineService;

import lombok.RequiredArgsConstructor;

// Rotinas de treino reutilizaveis e gerador automatico
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;
    private final CurrentUser currentUser;

    @GetMapping("/users/{userId}/routines")
    public List<RoutineResponseDTO> findByUser(@PathVariable Long userId) {
        currentUser.checkAccess(userId);
        return routineService.findByUser(userId);
    }

    @PostMapping("/users/{userId}/routines")
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponseDTO create(@PathVariable Long userId, @RequestBody RoutineRequestDTO dto) {
        currentUser.checkAccess(userId);
        return routineService.create(userId, dto);
    }

    // Monta rotinas pelo objetivo, dias por semana, nivel e equipamentos
    @PostMapping("/users/{userId}/routines/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RoutineResponseDTO> generate(@PathVariable Long userId, @RequestBody GenerateRoutineRequestDTO dto) {
        currentUser.checkAccess(userId);
        return routineService.generate(userId, dto);
    }

    @GetMapping("/routines/{id}")
    public RoutineResponseDTO findById(@PathVariable Long id) {
        return routineService.findById(id);
    }

    @PatchMapping("/routines/{id}")
    public RoutineResponseDTO update(@PathVariable Long id, @RequestBody RoutineRequestDTO dto) {
        return routineService.update(id, dto);
    }

    @DeleteMapping("/routines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        routineService.delete(id);
    }

    // Cria um treino com os exercicios da rotina (corpo opcional: {"date": "yyyy-MM-dd"})
    @PostMapping("/routines/{id}/start")
    @ResponseStatus(HttpStatus.CREATED)
    public StartedWorkoutDTO start(@PathVariable Long id, @RequestBody(required = false) StartRoutineRequestDTO dto) {
        return routineService.start(id, dto != null ? dto.date() : null);
    }

    // ---------- Exercicios da rotina ----------

    @PostMapping("/routines/{id}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponseDTO addExercise(@PathVariable Long id, @RequestBody RoutineExerciseRequestDTO dto) {
        return routineService.addExercise(id, dto);
    }

    @PatchMapping("/routine-exercises/{itemId}")
    public RoutineResponseDTO updateExercise(@PathVariable Long itemId, @RequestBody RoutineExerciseRequestDTO dto) {
        return routineService.updateExercise(itemId, dto);
    }

    @DeleteMapping("/routine-exercises/{itemId}")
    public RoutineResponseDTO removeExercise(@PathVariable Long itemId) {
        return routineService.removeExercise(itemId);
    }
}
