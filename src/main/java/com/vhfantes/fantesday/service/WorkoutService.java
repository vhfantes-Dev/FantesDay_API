package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.WorkoutExerciseRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutExerciseResponseDTO;
import com.vhfantes.fantesday.dto.WorkoutRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutResponseDTO;
import com.vhfantes.fantesday.dto.WorkoutSetRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutSetResponseDTO;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.entity.Workout;
import com.vhfantes.fantesday.entity.WorkoutExercise;
import com.vhfantes.fantesday.entity.WorkoutSet;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.WorkoutExerciseRepository;
import com.vhfantes.fantesday.repository.WorkoutRepository;
import com.vhfantes.fantesday.repository.WorkoutSetRepository;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.strength.StrengthCalculator;
import com.vhfantes.fantesday.strength.StrengthStandards;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final ProgressService progressService;
    private final StrengthService strengthService;
    private final StrengthStandards standards;
    private final CurrentUser currentUser;

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

    // Remove as series, os exercicios do treino, o treino e desconta o XP
    @Transactional
    public void delete(Long id) {
        Workout workout = getWorkoutOrThrow(id);
        workoutExerciseRepository.findByWorkoutId(id).forEach(this::deleteWithSets);
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

        return WorkoutExerciseResponseDTO.from(workoutExerciseRepository.save(item));
    }

    // Troca o exercicio; as series ja registradas passam a contar para o novo exercicio
    @Transactional
    public WorkoutExerciseResponseDTO updateExercise(Long itemId, WorkoutExerciseRequestDTO dto) {
        WorkoutExercise item = getItemOrThrow(itemId);

        if (dto.exerciseId() != null) {
            item.setExercise(exerciseService.getExerciseOrThrow(dto.exerciseId()));
        }

        return WorkoutExerciseResponseDTO.from(item);
    }

    @Transactional
    public void removeExercise(Long itemId) {
        deleteWithSets(getItemOrThrow(itemId));
    }

    // ---------- Series ----------

    @Transactional(readOnly = true)
    public List<WorkoutSetResponseDTO> findSets(Long itemId) {
        getItemOrThrow(itemId);
        return workoutSetRepository.findByWorkoutExerciseIdOrderByIdAsc(itemId).stream()
                .map(WorkoutSetResponseDTO::from)
                .toList();
    }

    // Registra a serie, estima o 1RM, marca se foi recorde pessoal e devolve o rank atualizado no exercicio
    @Transactional
    public WorkoutSetResponseDTO addSet(Long itemId, WorkoutSetRequestDTO dto) {
        validateReps(dto.reps());
        validateWeight(dto.weightKg());

        WorkoutExercise item = getItemOrThrow(itemId);

        WorkoutSet set = new WorkoutSet();
        set.setWorkoutExercise(item);
        set.setReps(dto.reps());
        set.setWeightKg(dto.weightKg());
        set = workoutSetRepository.save(set);

        applyEstimate(set);

        User user = item.getWorkout().getUser();
        return WorkoutSetResponseDTO.from(set, strengthService.rankForExercise(user, item.getExercise()));
    }

    // Se reps ou carga mudarem, recalcula o 1RM e o recorde
    @Transactional
    public WorkoutSetResponseDTO updateSet(Long setId, WorkoutSetRequestDTO dto) {
        WorkoutSet set = getSetOrThrow(setId);

        if (dto.reps() != null) {
            validateReps(dto.reps());
            set.setReps(dto.reps());
        }
        if (dto.weightKg() != null) {
            validateWeight(dto.weightKg());
            set.setWeightKg(dto.weightKg());
        }

        applyEstimate(set);

        WorkoutExercise item = set.getWorkoutExercise();
        return WorkoutSetResponseDTO.from(set, strengthService.rankForExercise(item.getWorkout().getUser(), item.getExercise()));
    }

    @Transactional
    public void removeSet(Long setId) {
        workoutSetRepository.delete(getSetOrThrow(setId));
    }

    // 1RM estimado + recorde pessoal (comparado com as outras series do usuario no mesmo exercicio)
    private void applyEstimate(WorkoutSet set) {
        WorkoutExercise item = set.getWorkoutExercise();
        User user = item.getWorkout().getUser();
        Exercise exercise = item.getExercise();

        BigDecimal estimate = StrengthCalculator.estimateOneRepMax(loadFor(user, exercise, set.getWeightKg()), set.getReps());
        set.setEstimatedOneRepMax(estimate);

        BigDecimal previousBest = workoutSetRepository.findBestOneRepMax(user.getId(), exercise.getId(), set.getId());
        set.setPersonalRecord(estimate != null && (previousBest == null || estimate.compareTo(previousBest) > 0));
    }

    // Em exercicios com peso corporal, a carga e peso corporal + carga extra (sem peso corporal, nao da para estimar)
    private BigDecimal loadFor(User user, Exercise exercise, BigDecimal weightKg) {
        if (!standards.isBodyweight(exercise.getExternalId())) {
            return weightKg;
        }
        if (user.getBodyweightKg() == null) {
            return null;
        }
        return weightKg != null ? user.getBodyweightKg().add(weightKg) : user.getBodyweightKg();
    }

    private void deleteWithSets(WorkoutExercise item) {
        workoutSetRepository.deleteAll(workoutSetRepository.findByWorkoutExerciseIdOrderByIdAsc(item.getId()));
        workoutExerciseRepository.delete(item);
    }

    private void validateReps(Integer reps) {
        if (reps == null || reps <= 0) {
            throw new BusinessException("Repeticoes devem ser maiores que zero");
        }
    }

    private void validateWeight(BigDecimal weightKg) {
        if (weightKg != null && weightKg.signum() < 0) {
            throw new BusinessException("Carga nao pode ser negativa");
        }
    }

    // So o dono do treino pode acessa-lo (ou acessar seus exercicios e series)
    private Workout getWorkoutOrThrow(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treino nao encontrado: " + id));
        currentUser.checkAccess(workout.getUser().getId());
        return workout;
    }

    private WorkoutExercise getItemOrThrow(Long id) {
        WorkoutExercise item = workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercicio do treino nao encontrado: " + id));
        currentUser.checkAccess(item.getWorkout().getUser().getId());
        return item;
    }

    private WorkoutSet getSetOrThrow(Long id) {
        WorkoutSet set = workoutSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serie nao encontrada: " + id));
        currentUser.checkAccess(set.getWorkoutExercise().getWorkout().getUser().getId());
        return set;
    }
}
