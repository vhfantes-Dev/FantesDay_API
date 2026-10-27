package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.GenerateRoutineRequestDTO;
import com.vhfantes.fantesday.dto.RoutineExerciseRequestDTO;
import com.vhfantes.fantesday.dto.RoutineExerciseResponseDTO;
import com.vhfantes.fantesday.dto.RoutineRequestDTO;
import com.vhfantes.fantesday.dto.RoutineResponseDTO;
import com.vhfantes.fantesday.dto.StartedWorkoutDTO;
import com.vhfantes.fantesday.dto.WorkoutExerciseRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutExerciseResponseDTO;
import com.vhfantes.fantesday.dto.WorkoutRequestDTO;
import com.vhfantes.fantesday.dto.WorkoutResponseDTO;
import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.entity.RoutineExercise;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.entity.WorkoutRoutine;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.repository.RoutineExerciseRepository;
import com.vhfantes.fantesday.repository.WorkoutRoutineRepository;
import com.vhfantes.fantesday.routine.EquipmentType;
import com.vhfantes.fantesday.routine.RoutineGenerator;
import com.vhfantes.fantesday.routine.TrainingGoal;
import com.vhfantes.fantesday.routine.TrainingLevel;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.strength.StrengthStandards;

import lombok.RequiredArgsConstructor;

// Rotinas de treino (modelos reutilizaveis), gerador automatico e "iniciar treino a partir da rotina"
@Service
@RequiredArgsConstructor
public class RoutineService {

    private static final String STRENGTH_CATEGORY = "Força";

    private final WorkoutRoutineRepository routineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final UserService userService;
    private final StrengthStandards strengthStandards;
    private final CurrentUser currentUser;

    // ---------- Rotinas ----------

    @Transactional(readOnly = true)
    public List<RoutineResponseDTO> findByUser(Long userId) {
        List<WorkoutRoutine> routines = routineRepository.findByUserIdOrderByNameAsc(userId);
        Map<Long, List<RoutineExercise>> itemsByRoutine = routineExerciseRepository
                .findByRoutineIdInOrderByPositionAsc(routines.stream().map(WorkoutRoutine::getId).toList()).stream()
                .collect(Collectors.groupingBy(item -> item.getRoutine().getId()));
        return routines.stream()
                .map(routine -> toResponse(routine, itemsByRoutine.getOrDefault(routine.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoutineResponseDTO findById(Long id) {
        WorkoutRoutine routine = getRoutineOrThrow(id);
        return toResponse(routine, routineExerciseRepository.findByRoutineIdOrderByPositionAsc(id));
    }

    @Transactional
    public RoutineResponseDTO create(Long userId, RoutineRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new BusinessException("Nome da rotina e obrigatorio");
        }

        WorkoutRoutine routine = new WorkoutRoutine();
        routine.setUser(userService.getUserOrThrow(userId));
        routine.setName(dto.name().trim());
        routine.setNotes(dto.notes());
        routine = routineRepository.saveAndFlush(routine);

        List<RoutineExercise> items = new ArrayList<>();
        if (dto.exercises() != null) {
            for (RoutineExerciseRequestDTO item : dto.exercises()) {
                items.add(saveItem(routine, item, items.size()));
            }
        }
        return toResponse(routine, items);
    }

    @Transactional
    public RoutineResponseDTO update(Long id, RoutineRequestDTO dto) {
        WorkoutRoutine routine = getRoutineOrThrow(id);
        if (dto.name() != null && !dto.name().isBlank()) {
            routine.setName(dto.name().trim());
        }
        if (dto.notes() != null) {
            routine.setNotes(dto.notes());
        }
        return toResponse(routine, routineExerciseRepository.findByRoutineIdOrderByPositionAsc(id));
    }

    @Transactional
    public void delete(Long id) {
        WorkoutRoutine routine = getRoutineOrThrow(id);
        routineExerciseRepository.deleteAll(routineExerciseRepository.findByRoutineIdOrderByPositionAsc(id));
        routineRepository.delete(routine);
    }

    // ---------- Exercicios da rotina ----------

    // Adiciona no fim (ou na posicao informada)
    @Transactional
    public RoutineResponseDTO addExercise(Long routineId, RoutineExerciseRequestDTO dto) {
        WorkoutRoutine routine = getRoutineOrThrow(routineId);
        List<RoutineExercise> items = new ArrayList<>(routineExerciseRepository.findByRoutineIdOrderByPositionAsc(routineId));

        RoutineExercise item = saveItem(routine, dto, items.size());
        items.add(item);
        if (dto.position() != null) {
            move(items, item, dto.position());
        }
        return toResponse(routine, items);
    }

    // Altera metas, troca o exercicio ou muda a posicao
    @Transactional
    public RoutineResponseDTO updateExercise(Long itemId, RoutineExerciseRequestDTO dto) {
        RoutineExercise item = getItemOrThrow(itemId);
        WorkoutRoutine routine = item.getRoutine();

        if (dto.exerciseId() != null) {
            item.setExercise(exerciseService.getExerciseOrThrow(dto.exerciseId()));
        }
        applyTargets(item, dto);

        List<RoutineExercise> items = new ArrayList<>(routineExerciseRepository.findByRoutineIdOrderByPositionAsc(routine.getId()));
        if (dto.position() != null) {
            move(items, item, dto.position());
        }
        return toResponse(routine, items);
    }

    @Transactional
    public RoutineResponseDTO removeExercise(Long itemId) {
        RoutineExercise item = getItemOrThrow(itemId);
        WorkoutRoutine routine = item.getRoutine();
        routineExerciseRepository.delete(item);

        List<RoutineExercise> items = new ArrayList<>(routineExerciseRepository.findByRoutineIdOrderByPositionAsc(routine.getId()));
        items.removeIf(other -> other.getId().equals(itemId));
        renumber(items);
        return toResponse(routine, items);
    }

    // ---------- Iniciar treino ----------

    // Cria um treino com os exercicios da rotina, na mesma ordem; as series sao registradas depois
    @Transactional
    public StartedWorkoutDTO start(Long routineId, LocalDate date) {
        WorkoutRoutine routine = getRoutineOrThrow(routineId);
        List<RoutineExercise> items = routineExerciseRepository.findByRoutineIdOrderByPositionAsc(routineId);
        if (items.isEmpty()) {
            throw new BusinessException("A rotina nao tem exercicios");
        }

        WorkoutResponseDTO workout = workoutService.create(routine.getUser().getId(),
                new WorkoutRequestDTO(routine.getName(), date != null ? date : LocalDate.now(), null));

        List<StartedWorkoutDTO.PlannedExercise> planned = new ArrayList<>();
        for (RoutineExercise item : items) {
            WorkoutExerciseResponseDTO added = workoutService.addExercise(workout.id(),
                    new WorkoutExerciseRequestDTO(item.getExercise().getId()));
            planned.add(new StartedWorkoutDTO.PlannedExercise(added.id(), added.exerciseId(), added.exerciseName(),
                    item.getTargetSets(), item.getTargetReps(), item.getRestSeconds()));
        }
        return new StartedWorkoutDTO(workout, planned);
    }

    // ---------- Gerador ----------

    @Transactional
    public List<RoutineResponseDTO> generate(Long userId, GenerateRoutineRequestDTO dto) {
        if (dto.daysPerWeek() == null) {
            throw new BusinessException("daysPerWeek e obrigatorio (1 a 6)");
        }
        TrainingGoal goal = dto.goal() != null ? dto.goal() : TrainingGoal.HYPERTROPHY;
        TrainingLevel level = dto.level() != null ? dto.level() : TrainingLevel.BEGINNER;
        Set<EquipmentType> equipment = dto.equipment() == null || dto.equipment().isEmpty()
                ? EnumSet.allOf(EquipmentType.class)
                : EnumSet.copyOf(dto.equipment());

        List<RoutineGenerator.PlannedRoutine> plans = RoutineGenerator.generate(goal, level, dto.daysPerWeek(),
                candidatesByMuscle(equipment));

        boolean save = dto.save() == null || dto.save();
        User user = userService.getUserOrThrow(userId);

        List<RoutineResponseDTO> result = new ArrayList<>();
        for (RoutineGenerator.PlannedRoutine plan : plans) {
            if (save) {
                List<RoutineExerciseRequestDTO> items = plan.exercises().stream()
                        .map(p -> new RoutineExerciseRequestDTO(p.exercise().getId(), p.sets(), p.reps(), p.restSeconds(), null))
                        .toList();
                result.add(create(user.getId(), new RoutineRequestDTO(plan.name(), generatedNotes(goal, level), items)));
            } else {
                List<RoutineExerciseResponseDTO> items = new ArrayList<>();
                for (RoutineGenerator.PlannedExercise p : plan.exercises()) {
                    Exercise exercise = p.exercise();
                    items.add(new RoutineExerciseResponseDTO(null, exercise.getId(), exercise.getName(),
                            exercise.getMuscleGroup(), exercise.getImageUrl(), items.size(), p.sets(), p.reps(), p.restSeconds()));
                }
                result.add(new RoutineResponseDTO(null, userId, plan.name(), generatedNotes(goal, level), items, null, null));
            }
        }
        return result;
    }

    // Candidatos por musculo: primeiro os exercicios com rank de forca (lista curada, na ordem do arquivo),
    // depois os demais exercicios de musculacao do catalogo (free-exercise-db e wger); todos filtrados pelos equipamentos
    private Map<String, List<Exercise>> candidatesByMuscle(Set<EquipmentType> equipment) {
        Map<String, Exercise> rankedById = exerciseRepository.findByExternalIdIn(strengthStandards.exerciseIds()).stream()
                .collect(Collectors.toMap(Exercise::getExternalId, Function.identity()));

        List<Exercise> ordered = new ArrayList<>();
        for (String externalId : strengthStandards.orderedExerciseIds()) {
            Exercise exercise = rankedById.get(externalId);
            if (exercise != null) {
                ordered.add(exercise);
            }
        }
        Set<Long> rankedIds = new HashSet<>(ordered.stream().map(Exercise::getId).toList());
        exerciseRepository.findBySourceInAndCategoryOrderByNameAsc(
                        List.of(CatalogSource.FREE_EXERCISE_DB, CatalogSource.WGER), STRENGTH_CATEGORY).stream()
                .filter(exercise -> !rankedIds.contains(exercise.getId()))
                .forEach(ordered::add);

        Map<String, List<Exercise>> byMuscle = new LinkedHashMap<>();
        for (Exercise exercise : ordered) {
            EquipmentType type = EquipmentType.fromCatalog(exercise.getEquipment());
            if (type != null && equipment.contains(type) && exercise.getMuscleGroup() != null) {
                byMuscle.computeIfAbsent(exercise.getMuscleGroup(), key -> new ArrayList<>()).add(exercise);
            }
        }
        return byMuscle;
    }

    private String generatedNotes(TrainingGoal goal, TrainingLevel level) {
        return "Gerada automaticamente (objetivo " + goal + ", nivel " + level + ")";
    }

    private RoutineExercise saveItem(WorkoutRoutine routine, RoutineExerciseRequestDTO dto, int position) {
        if (dto.exerciseId() == null) {
            throw new BusinessException("Exercicio e obrigatorio");
        }
        RoutineExercise item = new RoutineExercise();
        item.setRoutine(routine);
        item.setExercise(exerciseService.getExerciseOrThrow(dto.exerciseId()));
        item.setPosition(position);
        applyTargets(item, dto);
        return routineExerciseRepository.save(item);
    }

    private void applyTargets(RoutineExercise item, RoutineExerciseRequestDTO dto) {
        if (dto.targetSets() != null) {
            if (dto.targetSets() < 1 || dto.targetSets() > 20) {
                throw new BusinessException("Series devem estar entre 1 e 20");
            }
            item.setTargetSets(dto.targetSets());
        }
        if (dto.targetReps() != null) {
            item.setTargetReps(dto.targetReps().trim());
        }
        if (dto.restSeconds() != null) {
            if (dto.restSeconds() < 0 || dto.restSeconds() > 900) {
                throw new BusinessException("Descanso deve estar entre 0 e 900 segundos");
            }
            item.setRestSeconds(dto.restSeconds());
        }
    }

    // Move o item para a posicao pedida e renumera os demais (0, 1, 2...)
    private void move(List<RoutineExercise> items, RoutineExercise item, int position) {
        items.removeIf(other -> other.getId().equals(item.getId()));
        items.add(Math.max(0, Math.min(position, items.size())), item);
        renumber(items);
    }

    private void renumber(List<RoutineExercise> items) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i);
        }
    }

    private RoutineResponseDTO toResponse(WorkoutRoutine routine, List<RoutineExercise> items) {
        return new RoutineResponseDTO(routine.getId(), routine.getUser().getId(), routine.getName(), routine.getNotes(),
                items.stream().map(RoutineExerciseResponseDTO::from).toList(), routine.getCreatedAt(), routine.getUpdatedAt());
    }

    // So o dono da rotina pode acessa-la
    private WorkoutRoutine getRoutineOrThrow(Long id) {
        WorkoutRoutine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rotina nao encontrada: " + id));
        currentUser.checkAccess(routine.getUser().getId());
        return routine;
    }

    private RoutineExercise getItemOrThrow(Long id) {
        RoutineExercise item = routineExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercicio da rotina nao encontrado: " + id));
        currentUser.checkAccess(item.getRoutine().getUser().getId());
        return item;
    }
}
