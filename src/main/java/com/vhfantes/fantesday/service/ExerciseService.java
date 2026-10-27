package com.vhfantes.fantesday.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ExerciseRequestDTO;
import com.vhfantes.fantesday.dto.ExerciseResponseDTO;
import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ForbiddenException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.strength.StrengthStandards;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final StrengthStandards strengthStandards;

    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> findAll() {
        return exerciseRepository.findAll().stream()
                .map(ExerciseResponseDTO::from)
                .toList();
    }

    // Pesquisa por parte do nome, sem diferenciar maiusculas/minusculas
    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> searchByName(String name) {
        return exerciseRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ExerciseResponseDTO::from)
                .toList();
    }

    // Grupo muscular em portugues (ex: "Peito", "Dorsais", "Quadríceps")
    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> findByMuscleGroup(String muscleGroup) {
        return exerciseRepository.findByMuscleGroupIgnoreCase(muscleGroup).stream()
                .map(ExerciseResponseDTO::from)
                .toList();
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
        exerciseRepository.delete(getEditableExerciseOrThrow(id));
    }

    // Exercicios importados (free-exercise-db) sao compartilhados: so os cadastrados manualmente podem mudar
    private Exercise getEditableExerciseOrThrow(Long id) {
        Exercise exercise = getExerciseOrThrow(id);
        if (exercise.getSource() != null && exercise.getSource() != CatalogSource.CUSTOM) {
            throw new ForbiddenException("Exercicios do catalogo (" + exercise.getSource() + ") nao podem ser alterados");
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
