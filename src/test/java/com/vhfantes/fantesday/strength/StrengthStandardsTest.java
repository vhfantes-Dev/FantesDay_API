package com.vhfantes.fantesday.strength;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// Garante que o arquivo de padroes de forca esta consistente com o catalogo de exercicios
class StrengthStandardsTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void everyStandardPointsToAnExistingExercise() throws IOException {
        Set<String> catalogIds;
        try (InputStream input = new ClassPathResource("seed/exercises.json").getInputStream()) {
            List<Map<String, Object>> exercises = jsonMapper.readValue(input, new TypeReference<List<Map<String, Object>>>() {
            });
            catalogIds = exercises.stream().map(exercise -> (String) exercise.get("id")).collect(Collectors.toSet());
        }

        List<StrengthStandard> standards = StrengthStandards.load(jsonMapper);

        assertThat(standards).isNotEmpty();
        assertThat(standards).extracting(StrengthStandard::exerciseId).allMatch(catalogIds::contains).doesNotHaveDuplicates();
    }

    @Test
    void everyStandardHasFiveIncreasingLevels() {
        for (StrengthStandard standard : StrengthStandards.load(jsonMapper)) {
            assertIncreasing(standard.exerciseId(), standard.male());
            assertIncreasing(standard.exerciseId(), standard.female());
        }
    }

    private void assertIncreasing(String exerciseId, List<Double> levels) {
        assertThat(levels).as(exerciseId).hasSize(5);
        for (int i = 1; i < levels.size(); i++) {
            assertThat(levels.get(i)).as(exerciseId).isGreaterThan(levels.get(i - 1));
        }
    }
}
