package com.vhfantes.fantesday.task;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vhfantes.fantesday.exception.BusinessException;

// Converte o campo weekDays das tarefas ("SEG,QUA,SEX") em dias da semana.
// Aceita abreviacoes em portugues (SEG, TER, QUA, QUI, SEX, SAB, DOM) ou ingles (MON ... SUN),
// com ou sem acento, em qualquer caixa. Salva sempre no formato em portugues.
public final class WeekDays {

    private static final Map<String, DayOfWeek> ALIASES = Map.ofEntries(
            Map.entry("SEG", DayOfWeek.MONDAY), Map.entry("MON", DayOfWeek.MONDAY),
            Map.entry("TER", DayOfWeek.TUESDAY), Map.entry("TUE", DayOfWeek.TUESDAY),
            Map.entry("QUA", DayOfWeek.WEDNESDAY), Map.entry("WED", DayOfWeek.WEDNESDAY),
            Map.entry("QUI", DayOfWeek.THURSDAY), Map.entry("THU", DayOfWeek.THURSDAY),
            Map.entry("SEX", DayOfWeek.FRIDAY), Map.entry("FRI", DayOfWeek.FRIDAY),
            Map.entry("SAB", DayOfWeek.SATURDAY), Map.entry("SAT", DayOfWeek.SATURDAY),
            Map.entry("DOM", DayOfWeek.SUNDAY), Map.entry("SUN", DayOfWeek.SUNDAY));

    private static final Map<DayOfWeek, String> LABELS = Map.of(
            DayOfWeek.MONDAY, "SEG", DayOfWeek.TUESDAY, "TER", DayOfWeek.WEDNESDAY, "QUA",
            DayOfWeek.THURSDAY, "QUI", DayOfWeek.FRIDAY, "SEX", DayOfWeek.SATURDAY, "SAB",
            DayOfWeek.SUNDAY, "DOM");

    private WeekDays() {
    }

    // null ou vazio = sem repeticao (conjunto vazio)
    public static Set<DayOfWeek> parse(String value) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (value == null || value.isBlank()) {
            return days;
        }

        for (String part : value.split("[,;\\s]+")) {
            if (part.isBlank()) {
                continue;
            }
            DayOfWeek day = ALIASES.get(stripAccents(part.trim()).toUpperCase());
            if (day == null) {
                throw new BusinessException("Dia da semana invalido: " + part
                        + " (use SEG, TER, QUA, QUI, SEX, SAB, DOM)");
            }
            days.add(day);
        }
        return days;
    }

    // Formato salvo no banco: dias em ordem, separados por virgula (null se nao houver dias)
    public static String normalize(String value) {
        Set<DayOfWeek> days = parse(value);
        if (days.isEmpty()) {
            return null;
        }
        return days.stream().map(LABELS::get).collect(Collectors.joining(","));
    }

    private static String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
