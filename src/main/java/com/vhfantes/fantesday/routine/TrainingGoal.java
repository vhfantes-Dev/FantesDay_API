package com.vhfantes.fantesday.routine;

// Objetivo do treino gerado: define series, faixa de repeticoes e descanso
public enum TrainingGoal {
    STRENGTH(4, "3-6", 180),
    HYPERTROPHY(3, "8-12", 90),
    ENDURANCE(3, "15-20", 45);

    private final int baseSets;
    private final String reps;
    private final int restSeconds;

    TrainingGoal(int baseSets, String reps, int restSeconds) {
        this.baseSets = baseSets;
        this.reps = reps;
        this.restSeconds = restSeconds;
    }

    public int baseSets() {
        return baseSets;
    }

    public String reps() {
        return reps;
    }

    public int restSeconds() {
        return restSeconds;
    }
}
