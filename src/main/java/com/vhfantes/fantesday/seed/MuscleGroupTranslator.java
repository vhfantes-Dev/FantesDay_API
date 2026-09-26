package com.vhfantes.fantesday.seed;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Traduz os nomes de musculos do free-exercise-db (ingles) para portugues
public final class MuscleGroupTranslator {

    private static final Map<String, String> TRANSLATIONS = Map.ofEntries(
            Map.entry("abdominals", "Abdômen"),
            Map.entry("abductors", "Abdutores"),
            Map.entry("adductors", "Adutores"),
            Map.entry("biceps", "Bíceps"),
            Map.entry("calves", "Panturrilhas"),
            Map.entry("chest", "Peito"),
            Map.entry("forearms", "Antebraços"),
            Map.entry("glutes", "Glúteos"),
            Map.entry("hamstrings", "Posteriores de coxa"),
            Map.entry("lats", "Dorsais"),
            Map.entry("lower back", "Lombar"),
            Map.entry("middle back", "Costas"),
            Map.entry("neck", "Pescoço"),
            Map.entry("quadriceps", "Quadríceps"),
            Map.entry("shoulders", "Ombros"),
            Map.entry("traps", "Trapézio"),
            Map.entry("triceps", "Tríceps"));

    private MuscleGroupTranslator() {
    }

    // Se nao houver traducao, devolve o nome original
    public static String translate(String muscle) {
        if (muscle == null) {
            return null;
        }
        return TRANSLATIONS.getOrDefault(muscle.trim().toLowerCase(), muscle);
    }

    // Traduz uma lista e junta separando por virgula (null se a lista estiver vazia)
    public static String translateAll(List<String> muscles) {
        if (muscles == null || muscles.isEmpty()) {
            return null;
        }
        return muscles.stream()
                .map(MuscleGroupTranslator::translate)
                .collect(Collectors.joining(", "));
    }
}
