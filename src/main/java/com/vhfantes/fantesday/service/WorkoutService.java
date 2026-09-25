package com.vhfantes.fantesday.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.WorkoutExerciseRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutExerciseResponseDTO;
import com.vhfantes.fantesday.dto.WorkoutRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutResponseDTO;
import com.vhfantes.fantesday.entity.Workout;
import com.vhfantes.fantesday.entity.WorkoutExercise;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.WorkoutExerciseRepository;
import com.vhfantes.fantesday.repository.WorkoutRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final ProgressService progressService;

    // ---------- Treinos ----------

    @Transactional(readOnly = true)
    public List<WorkoutResponseDTO> findByUser(Long userId) {
        return workoutRepository.findByUserId(userId).stream()
                .map(WorkoutResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponseDTO findById(Long id) {
        return WorkoutResponseDTO.from(getWorkoutOrThrow(id));
    }

    // Cria o treino e soma o XP ganho no progresso do usuario
    @Transactional
    public WorkoutResponseDTO create(Long userId, WorkoutRequestDTO dto) {
        Workout workout = new Workout();
        workout.setUser(userService.getUserOrThrow(userId));
        workout.setWorkoutType(dto.workoutType());
        workout.setDate(dto.date());
        if (dto.xpEarned() != null) {
            workout.setXpEarned(dto.xpEarned());
        }
        workout = workoutRepository.save(workout);

        progressService.addXp(userId, workout.getXpEarned());

        return WorkoutResponseDTO.from(workout);
    }

    // Atualiza apenas os campos enviados; se o XP mudar, ajusta a diferenca no progresso
    @Transactional
    public WorkoutResponseDTO update(Long id, WorkoutRequestDTO dto) {
        Workout workout = getWorkoutOrThrow(id);

        if (dto.workoutType() != null) {
            workout.setWorkoutType(dto.workoutType());
        }
        if (dto.date() != null) {
            workout.setDate(dto.date());
        }
        if (dto.xpEarned() != null) {
            int difference = dto.xpEarned() - workout.getXpEarned();
            workout.setXpEarned(dto.xpEarned());
            progressService.addXp(workout.getUser().getId(), difference);
        }

        return WorkoutResponseDTO.from(workout);
    }

    // Remove os exercicios do treino, o treino e desconta o XP
    @Transactional
    public void delete(Long id) {
        Workout workout = getWorkoutOrThrow(id);
        workoutExerciseRepository.deleteAll(workoutExerciseRepository.findByWorkoutId(id));
        progressService.addXp(workout.getUser().getId(), -workout.getXpEarned());
        workoutRepository.delete(workout);
    }

    // ---------- Exercicios do treino ----------

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponseDTO> findExercises(Long workoutId) {
        getWorkoutOrThrow(workoutId);
        return workoutExerciseRepository.findByWorkoutId(workoutId).stream()
                .map(WorkoutExerciseResponseDTO::from)
                .toList();
    }

    @Transactional
    public WorkoutExerciseResponseDTO addExercise(Long workoutId, WorkoutExerciseRequestDTO dto) {
        if (dto.exerciseId() == null) {
            throw new BusinessException("Exercicio e obrigatorio");
        }

        WorkoutExercise item = new WorkoutExercise();
        item.setWorkout(getWorkoutOrThrow(workoutId));
        item.setExercise(exerciseService.getExerciseOrThrow(dto.exerciseId()));
        item.setSets(dto.sets());
        item.setReps(dto.reps());
        item.setWeightKg(dto.weightKg());

        return WorkoutExerciseResponseDTO.from(workoutExerciseRepository.save(item));
    }

    @Transactional
    public WorkoutExerciseResponseDTO updateExercise(Long itemId, WorkoutExerciseRequestDTO dto) {
        WorkoutExercise item = getItemOrThrow(itemId);

        if (dto.exerciseId() != null) {
            item.setExercise(exerciseService.getExerciseOrThrow(dto.exerciseId()));
        }
        if (dto.sets() != null) {
            item.setSets(dto.sets());
        }
        if (dto.reps() != null) {
            item.setReps(dto.reps());
        }
        if (dto.weightKg() != null) {
            item.setWeightKg(dto.weightKg());
        }

        return WorkoutExerciseResponseDTO.from(item);
    }

    @Transactional
    public void removeExercise(Long itemId) {
        workoutExerciseRepository.delete(getItemOrThrow(itemId));
    }

    private Workout getWorkoutOrThrow(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treino nao encontrado: " + id));
    }

    private WorkoutExercise getItemOrThrow(Long id) {
        return workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercicio do treino nao encontrado: " + id));
    }
}
