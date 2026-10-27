package com.vhfantes.fantesday.strength;

import java.util.List;

import com.vhfantes.fantesday.entity.Sex;

// Padrao de forca de um exercicio: razao 1RM / peso corporal em cada nivel
// (iniciante, novato, intermediario, avancado, elite), por sexo.
// Exercicios com halteres usam o peso de UM halter.
// Exercicios com peso corporal (bodyweight = true) usam (peso corporal + carga extra) / peso corporal.
public record StrengthStandard(
        String exerciseId,
        boolean bodyweight,
        List<Double> male,
        List<Double> female) {

    public List<Double> anchorsFor(Sex sex) {
        return sex == Sex.FEMALE ? female : male;
    }
}
