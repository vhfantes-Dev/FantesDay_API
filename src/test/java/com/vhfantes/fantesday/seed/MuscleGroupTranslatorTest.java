package com.vhfantes.fantesday.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class MuscleGroupTranslatorTest {

    @Test
    void translatesKnownMuscle() {
        assertThat(MuscleGroupTranslator.translate("chest")).isEqualTo("Peito");
        assertThat(MuscleGroupTranslator.translate("Middle Back")).isEqualTo("Costas");
    }

    @Test
    void keepsUnknownMuscle() {
        assertThat(MuscleGroupTranslator.translate("pinky toe")).isEqualTo("pinky toe");
        assertThat(MuscleGroupTranslator.translate(null)).isNull();
    }

    @Test
    void translatesList() {
        assertThat(MuscleGroupTranslator.translateAll(List.of("biceps", "forearms"))).isEqualTo("Bíceps, Antebraços");
        assertThat(MuscleGroupTranslator.translateAll(List.of())).isNull();
    }
}
