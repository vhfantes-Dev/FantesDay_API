package com.vhfantes.fantesday.routine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.exception.BusinessException;

class RoutineGeneratorTest {

    private long nextId = 1;

    @Test
    void threeDaysForIntermediateIsPushPullLegs() {
        List<RoutineGenerator.PlannedRoutine> routines = RoutineGenerator.generate(
                TrainingGoal.HYPERTROPHY, TrainingLevel.INTERMEDIATE, 3, fullCatalog());

        assertThat(routines).extracting(RoutineGenerator.PlannedRoutine::name)
                .containsExactly("Treino A - Push - Peito, Ombros e Tríceps", "Treino B - Pull - Costas e Bíceps",
                        "Treino C - Legs - Pernas");
        assertThat(routines).allSatisfy(routine -> assertThat(routine.exercises()).hasSize(5));
    }

    @Test
    void threeDaysForBeginnerIsFullBody() {
        List<RoutineGenerator.PlannedRoutine> routines = RoutineGenerator.generate(
                TrainingGoal.HYPERTROPHY, TrainingLevel.BEGINNER, 3, fullCatalog());

        assertThat(routines).extracting(RoutineGenerator.PlannedRoutine::name)
                .allMatch(name -> name.endsWith("Full Body"));
        assertThat(routines).allSatisfy(routine -> assertThat(routine.exercises()).hasSize(4));
    }

    @Test
    void setsRepsAndRestFollowGoalAndLevel() {
        RoutineGenerator.PlannedExercise first = RoutineGenerator.generate(
                TrainingGoal.STRENGTH, TrainingLevel.ADVANCED, 1, fullCatalog()).get(0).exercises().get(0);

        assertThat(first.sets()).isEqualTo(5); // 4 do objetivo + 1 do nivel avancado
        assertThat(first.reps()).isEqualTo("3-6");
        assertThat(first.restSeconds()).isEqualTo(180);
    }

    @Test
    void doesNotRepeatExerciseInTheSameDayAndVariesRepeatedDays() {
        List<RoutineGenerator.PlannedRoutine> routines = RoutineGenerator.generate(
                TrainingGoal.HYPERTROPHY, TrainingLevel.ADVANCED, 6, fullCatalog());

        for (RoutineGenerator.PlannedRoutine routine : routines) {
            List<Long> ids = routine.exercises().stream().map(e -> e.exercise().getId()).toList();
            assertThat(ids).doesNotHaveDuplicates();
        }
        // Push A e Push B comecam por exercicios de peito diferentes
        assertThat(routines.get(0).exercises().get(0).exercise().getId())
                .isNotEqualTo(routines.get(3).exercises().get(0).exercise().getId());
    }

    @Test
    void failsWhenADayHasNoExercise() {
        Map<String, List<Exercise>> onlyChest = Map.of(RoutineGenerator.CHEST, List.of(exercise(RoutineGenerator.CHEST)));

        // Push/Pull/Legs so com exercicio de quadriceps: o dia de Push fica vazio
        assertThatThrownBy(() -> RoutineGenerator.generate(TrainingGoal.HYPERTROPHY, TrainingLevel.INTERMEDIATE, 3,
                Map.of(RoutineGenerator.QUADS, List.of(exercise(RoutineGenerator.QUADS)))))
                .isInstanceOf(BusinessException.class);
        assertThat(RoutineGenerator.generate(TrainingGoal.HYPERTROPHY, TrainingLevel.BEGINNER, 1, onlyChest).get(0).exercises())
                .hasSize(1);
    }

    @Test
    void rejectsInvalidDaysPerWeek() {
        assertThatThrownBy(() -> RoutineGenerator.generate(TrainingGoal.HYPERTROPHY, TrainingLevel.BEGINNER, 7, fullCatalog()))
                .isInstanceOf(BusinessException.class);
    }

    // 3 exercicios para cada musculo usado pelo gerador
    private Map<String, List<Exercise>> fullCatalog() {
        Map<String, List<Exercise>> catalog = new HashMap<>();
        for (String muscle : List.of(RoutineGenerator.CHEST, RoutineGenerator.BACK, RoutineGenerator.LATS,
                RoutineGenerator.SHOULDERS, RoutineGenerator.BICEPS, RoutineGenerator.TRICEPS, RoutineGenerator.QUADS,
                RoutineGenerator.HAMSTRINGS, RoutineGenerator.GLUTES, RoutineGenerator.CALVES,
                RoutineGenerator.LOWER_BACK, RoutineGenerator.TRAPS)) {
            List<Exercise> exercises = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                exercises.add(exercise(muscle));
            }
            catalog.put(muscle, exercises);
        }
        return catalog;
    }

    private Exercise exercise(String muscle) {
        Exercise exercise = new Exercise();
        exercise.setId(nextId++);
        exercise.setName(muscle + " " + exercise.getId());
        exercise.setMuscleGroup(muscle);
        return exercise;
    }
}
