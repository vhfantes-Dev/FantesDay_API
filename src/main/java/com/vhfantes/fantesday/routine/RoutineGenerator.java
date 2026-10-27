package com.vhfantes.fantesday.routine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.exception.BusinessException;

// Gerador de rotinas por regras, sem banco (o service entrega os exercicios candidatos).
// 1. A divisao vem dos dias por semana: Full Body, Superiores/Inferiores ou Push/Pull/Legs.
// 2. Cada tipo de dia tem uma lista de grupos musculares ("slots"); o nivel define quantos slots entram.
// 3. Cada slot recebe o primeiro exercicio candidato daquele musculo ainda nao usado no dia;
//    dias repetidos (ex: Push A e Push B) comecam em outro ponto da lista, para variar.
// 4. Series, repeticoes e descanso vem do objetivo, com ajuste de series pelo nivel.
public final class RoutineGenerator {

    static final String CHEST = "Peito";
    static final String BACK = "Costas";
    static final String LATS = "Dorsais";
    static final String SHOULDERS = "Ombros";
    static final String BICEPS = "Bíceps";
    static final String TRICEPS = "Tríceps";
    static final String QUADS = "Quadríceps";
    static final String HAMSTRINGS = "Posteriores de coxa";
    static final String GLUTES = "Glúteos";
    static final String CALVES = "Panturrilhas";
    static final String LOWER_BACK = "Lombar";
    static final String TRAPS = "Trapézio";

    enum DayType {
        FULL_BODY("Full Body", List.of(QUADS, CHEST, BACK, SHOULDERS, HAMSTRINGS, BICEPS, TRICEPS)),
        UPPER("Superiores", List.of(CHEST, BACK, SHOULDERS, LATS, BICEPS, TRICEPS)),
        LOWER("Inferiores", List.of(QUADS, HAMSTRINGS, GLUTES, QUADS, CALVES, LOWER_BACK)),
        PUSH("Push - Peito, Ombros e Tríceps", List.of(CHEST, CHEST, SHOULDERS, SHOULDERS, TRICEPS, TRICEPS)),
        PULL("Pull - Costas e Bíceps", List.of(LATS, BACK, BACK, TRAPS, BICEPS, BICEPS)),
        LEGS("Legs - Pernas", List.of(QUADS, HAMSTRINGS, QUADS, GLUTES, CALVES, HAMSTRINGS));

        final String label;
        final List<String> slots;

        DayType(String label, List<String> slots) {
            this.label = label;
            this.slots = slots;
        }
    }

    private static final String[] LETTERS = { "A", "B", "C", "D", "E", "F" };

    private RoutineGenerator() {
    }

    public record PlannedExercise(Exercise exercise, int sets, String reps, int restSeconds) {
    }

    public record PlannedRoutine(String name, List<PlannedExercise> exercises) {
    }

    // candidatesByMuscle: exercicios ja filtrados pelo equipamento, na ordem de preferencia
    public static List<PlannedRoutine> generate(TrainingGoal goal, TrainingLevel level, int daysPerWeek,
            Map<String, List<Exercise>> candidatesByMuscle) {

        if (daysPerWeek < 1 || daysPerWeek > 6) {
            throw new BusinessException("Dias por semana deve estar entre 1 e 6");
        }

        List<DayType> split = split(daysPerWeek, level);
        int sets = Math.max(2, goal.baseSets() + level.extraSets());
        Map<DayType, Integer> timesSeen = new HashMap<>();

        List<PlannedRoutine> routines = new ArrayList<>();
        for (int day = 0; day < split.size(); day++) {
            DayType type = split.get(day);
            int variant = timesSeen.merge(type, 1, Integer::sum) - 1;

            List<PlannedExercise> exercises = new ArrayList<>();
            Set<Long> usedToday = new HashSet<>();
            for (String muscle : type.slots) {
                if (exercises.size() >= level.exercisesPerDay()) {
                    break;
                }
                Exercise pick = pick(candidatesByMuscle.getOrDefault(muscle, List.of()), variant, usedToday);
                if (pick != null) {
                    usedToday.add(pick.getId());
                    exercises.add(new PlannedExercise(pick, sets, goal.reps(), goal.restSeconds()));
                }
            }

            if (exercises.isEmpty()) {
                throw new BusinessException("Nao ha exercicios para o treino " + type.label
                        + " com os equipamentos escolhidos. Inclua mais equipamentos");
            }
            routines.add(new PlannedRoutine("Treino " + LETTERS[day] + " - " + type.label, exercises));
        }
        return routines;
    }

    static List<DayType> split(int daysPerWeek, TrainingLevel level) {
        return switch (daysPerWeek) {
            case 1 -> List.of(DayType.FULL_BODY);
            case 2 -> List.of(DayType.FULL_BODY, DayType.FULL_BODY);
            case 3 -> level == TrainingLevel.BEGINNER
                    ? List.of(DayType.FULL_BODY, DayType.FULL_BODY, DayType.FULL_BODY)
                    : List.of(DayType.PUSH, DayType.PULL, DayType.LEGS);
            case 4 -> List.of(DayType.UPPER, DayType.LOWER, DayType.UPPER, DayType.LOWER);
            case 5 -> List.of(DayType.PUSH, DayType.PULL, DayType.LEGS, DayType.UPPER, DayType.LOWER);
            default -> List.of(DayType.PUSH, DayType.PULL, DayType.LEGS, DayType.PUSH, DayType.PULL, DayType.LEGS);
        };
    }

    // Comeca em "variant" e da a volta na lista, pulando o que ja entrou no dia
    private static Exercise pick(List<Exercise> candidates, int variant, Set<Long> usedToday) {
        for (int i = 0; i < candidates.size(); i++) {
            Exercise candidate = candidates.get((variant + i) % candidates.size());
            if (!usedToday.contains(candidate.getId())) {
                return candidate;
            }
        }
        return null;
    }
}
