package com.vhfantes.fantesday.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ExerciseRequestDTO;
import com.vhfantes.fantesday.dto.ExerciseResponseDTO;
import com.vhfantes.fantesday.dto.PageResponseDTO;
import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ForbiddenException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.repository.RoutineExerciseRepository;
import com.vhfantes.fantesday.repository.WorkoutExerciseRepository;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.strength.StrengthStandards;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final StrengthStandards strengthStandards;
    private final UserService userService;
    private final CurrentUser currentUser;

    // Lista paginada; filtros opcionais por parte do nome e por grupo muscular em portugues
    // (ex: "Peito", "Dorsais", "Quadríceps")
    @Transactional(readOnly = true)
    public PageResponseDTO<ExerciseResponseDTO> search(String name, String muscleGroup, Pageable pageable) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasMuscle = muscleGroup != null && !muscleGroup.isBlank();

        Page<Exercise> page;
        if (hasName && hasMuscle) {
            page = exerciseRepository.findByNameContainingIgnoreCaseAndMuscleGroupIgnoreCase(name.trim(), muscleGroup.trim(), pageable);
        } else if (hasName) {
            page = exerciseRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else if (hasMuscle) {
            page = exerciseRepository.findByMuscleGroupIgnoreCase(muscleGroup.trim(), pageable);
        } else {
            page = exerciseRepository.findAll(pageable);
        }
        return PageResponseDTO.from(page, ExerciseResponseDTO::from);
    }

    // Exercicios da lista curada que tem rank de forca
    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> findRanked() {
        return exerciseRepository.findByExternalIdIn(strengthStandards.exerciseIds()).stream()
                .sorted(Comparator.comparing(Exercise::getMuscleGroup, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Exercise::getName))
                .map(ExerciseResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseResponseDTO findById(Long id) {
        return ExerciseResponseDTO.from(getExerciseOrThrow(id));
    }

    @Transactional
    public ExerciseResponseDTO create(ExerciseRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new BusinessException("Nome e obrigatorio");
        }

        Exercise exercise = new Exercise();
        exercise.setName(dto.name().trim());
        exercise.setMuscleGroup(dto.muscleGroup());
        exercise.setSecondaryMuscles(dto.secondaryMuscles());
        exercise.setEquipment(dto.equipment());
        exercise.setLevel(dto.level());
        exercise.setCategory(dto.category());
        exercise.setInstructions(dto.instructions());
        exercise.setImageUrl(dto.imageUrl());
        exercise.setSource(CatalogSource.CUSTOM);
        exercise.setCreatedBy(userService.getUserOrThrow(currentUser.id()));

        return ExerciseResponseDTO.from(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseResponseDTO update(Long id, ExerciseRequestDTO dto) {
        Exercise exercise = getEditableExerciseOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            exercise.setName(dto.name().trim());
        }
        if (dto.muscleGroup() != null) {
            exercise.setMuscleGroup(dto.muscleGroup());
        }
        if (dto.secondaryMuscles() != null) {
            exercise.setSecondaryMuscles(dto.secondaryMuscles());
        }
        if (dto.equipment() != null) {
            exercise.setEquipment(dto.equipment());
        }
        if (dto.level() != null) {
            exercise.setLevel(dto.level());
        }
        if (dto.category() != null) {
            exercise.setCategory(dto.category());
        }
        if (dto.instructions() != null) {
            exercise.setInstructions(dto.instructions());
        }
        if (dto.imageUrl() != null) {
            exercise.setImageUrl(dto.imageUrl());
        }

        return ExerciseResponseDTO.from(exercise);
    }

    @Transactional
    public void delete(Long id) {
        Exercise exercise = getEditableExerciseOrThrow(id);
        if (workoutExerciseRepository.existsByExerciseId(id) || routineExerciseRepository.existsByExerciseId(id)) {
            throw new BusinessException("Exercicio usado em treinos ou rotinas; remova-o deles antes de excluir");
        }
        exerciseRepository.delete(exercise);
    }

    // Exercicios importados (free-exercise-db) sao compartilhados e nao mudam;
    // os cadastrados manualmente so podem ser alterados por quem os criou
    private Exercise getEditableExerciseOrThrow(Long id) {
        Exercise exercise = getExerciseOrThrow(id);
        if (exercise.getSource() != null && exercise.getSource() != CatalogSource.CUSTOM) {
            throw new ForbiddenException("Exercicios do catalogo (" + exercise.getSource() + ") nao podem ser alterados");
        }
        if (exercise.getCreatedBy() == null || !exercise.getCreatedBy().getId().equals(currentUser.id())) {
            throw new ForbiddenException("Apenas quem cadastrou o exercicio pode altera-lo");
        }
        return exercise;
    }

    // Usado pelo WorkoutService para montar os exercicios de um treino
    @Transactional(readOnly = true)
    public Exercise getExerciseOrThrow(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercicio nao encontrado: " + id));
    }
}
