package com.vhfantes.fantesday.routine;

// Nivel do aluno: define quantos exercicios por dia e ajusta o numero de series
public enum TrainingLevel {
    BEGINNER(4, -1),
    INTERMEDIATE(5, 0),
    ADVANCED(6, 1);

    private final int exercisesPerDay;
    private final int extraSets;

    TrainingLevel(int exercisesPerDay, int extraSets) {
        this.exercisesPerDay = exercisesPerDay;
        this.extraSets = extraSets;
    }

    public int exercisesPerDay() {
        return exercisesPerDay;
    }

    public int extraSets() {
        return extraSets;
    }
}
