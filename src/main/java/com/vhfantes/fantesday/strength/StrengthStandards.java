package com.vhfantes.fantesday.strength;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// Carrega resources/seed/strength-standards.json: a lista curada de exercicios com rank.
// A chave e o externalId do exercicio no free-exercise-db.
@Component
public class StrengthStandards {

    public static final String FILE = "seed/strength-standards.json";

    private final Map<String, StrengthStandard> byExerciseId;

    public StrengthStandards(JsonMapper jsonMapper) {
        this.byExerciseId = load(jsonMapper).stream()
                .collect(Collectors.toUnmodifiableMap(StrengthStandard::exerciseId, Function.identity()));
    }

    public static List<StrengthStandard> load(JsonMapper jsonMapper) {
        try (InputStream input = new ClassPathResource(FILE).getInputStream()) {
            return jsonMapper.readValue(input, new TypeReference<List<StrengthStandard>>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + FILE, e);
        }
    }

    public Optional<StrengthStandard> find(String externalId) {
        return externalId == null ? Optional.empty() : Optional.ofNullable(byExerciseId.get(externalId));
    }

    public boolean isBodyweight(String externalId) {
        return find(externalId).map(StrengthStandard::bodyweight).orElse(false);
    }

    public Set<String> exerciseIds() {
        return byExerciseId.keySet();
    }
}
