package com.vhfantes.fantesday.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ExerciseRequestDTO;
import com.vhfantes.fantesday.dto.ExerciseResponseDTO;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> findAll() {
        return exerciseRepository.findAll().stream()
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

        return ExerciseResponseDTO.from(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseResponseDTO update(Long id, ExerciseRequestDTO dto) {
        Exercise exercise = getExerciseOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            exercise.setName(dto.name().trim());
        }
        if (dto.muscleGroup() != null) {
            exercise.setMuscleGroup(dto.muscleGroup());
        }

        return ExerciseResponseDTO.from(exercise);
    }

    @Transactional
    public void delete(Long id) {
        exerciseRepository.delete(getExerciseOrThrow(id));
    }

    // Usado pelo WorkoutService para montar os exercicios de um treino
    @Transactional(readOnly = true)
    public Exercise getExerciseOrThrow(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercicio nao encontrado: " + id));
    }
}
