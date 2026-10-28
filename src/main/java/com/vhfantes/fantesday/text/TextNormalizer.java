package com.vhfantes.fantesday.text;

import java.text.Normalizer;
import java.util.Locale;

// Deixa o texto comparavel: minusculo, sem acento e so com letras e numeros separados por um espaco
// ("Joguei Vôlei!" -> "joguei volei")
public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
