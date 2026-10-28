package com.vhfantes.fantesday.activity;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Le a duracao de um texto livre: "por 2 horas", "1h30", "uma hora e meia", "45 min", "meia hora"...
public final class ActivityTextParser {

    private static final Map<String, String> NUMBER_WORDS = Map.ofEntries(
            Map.entry("um", "1"), Map.entry("uma", "1"), Map.entry("dois", "2"), Map.entry("duas", "2"),
            Map.entry("tres", "3"), Map.entry("quatro", "4"), Map.entry("cinco", "5"), Map.entry("seis", "6"),
            Map.entry("sete", "7"), Map.entry("oito", "8"), Map.entry("nove", "9"), Map.entry("dez", "10"),
            Map.entry("quinze", "15"), Map.entry("vinte", "20"), Map.entry("trinta", "30"),
            Map.entry("quarenta", "40"), Map.entry("cinquenta", "50"));

    // "1h30", "1h30min"
    private static final Pattern COMPACT = Pattern.compile("(?<![\\d.,])(\\d+)h(\\d{1,2})(?:min)?\\b");
    // "2 horas", "2h", "1,5 hora"
    private static final Pattern HOURS = Pattern.compile("(?<![\\d.,])(\\d+(?:[.,]\\d+)?)\\s*(?:horas|hora|hrs|hr|h)\\b");
    // "45 min", "30 minutos"
    private static final Pattern MINUTES = Pattern.compile("(?<![\\d.,])(\\d+)\\s*(?:minutos|minuto|mins|min)\\b");
    // "quarenta e cinco" -> "40 e 5" -> "45"
    private static final Pattern TENS_AND_UNITS = Pattern.compile("\\b([2-5]0) e ([1-9])\\b");

    private ActivityTextParser() {
    }

    // Duracao em minutos, ou null se o texto nao tiver duracao
    public static Integer parseDurationMinutes(String text) {
        String normalized = normalize(text);

        Matcher compact = COMPACT.matcher(normalized);
        if (compact.find()) {
            return positiveOrNull(Integer.parseInt(compact.group(1)) * 60 + Integer.parseInt(compact.group(2)));
        }

        double total = 0;
        Matcher hours = HOURS.matcher(normalized);
        if (hours.find()) {
            total += Double.parseDouble(hours.group(1).replace(',', '.')) * 60;
            if (normalized.substring(hours.end()).startsWith(" e meia")) {
                total += 30;
            }
        }
        Matcher minutes = MINUTES.matcher(normalized);
        if (minutes.find()) {
            total += Integer.parseInt(minutes.group(1));
        }
        if (total == 0 && normalized.matches(".*\\bmeia hora\\b.*")) {
            total = 30;
        }
        return positiveOrNull((int) Math.round(total));
    }

    // Minusculo e sem acento, mantendo virgula e ponto dos numeros; numeros por extenso viram digitos
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9.,]+", " ")
                .trim();

        StringBuilder result = new StringBuilder();
        for (String word : normalized.split(" ")) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(NUMBER_WORDS.getOrDefault(word, word));
        }

        Matcher tensAndUnits = TENS_AND_UNITS.matcher(result);
        StringBuilder combined = new StringBuilder();
        while (tensAndUnits.find()) {
            int value = Integer.parseInt(tensAndUnits.group(1)) + Integer.parseInt(tensAndUnits.group(2));
            tensAndUnits.appendReplacement(combined, String.valueOf(value));
        }
        tensAndUnits.appendTail(combined);
        return combined.toString();
    }

    private static Integer positiveOrNull(int minutes) {
        return minutes > 0 ? minutes : null;
    }
}
