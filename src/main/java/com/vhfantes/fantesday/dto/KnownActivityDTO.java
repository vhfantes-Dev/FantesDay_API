package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.activity.KnownActivity;

// Atividade do catalogo com o MET usado no calculo das calorias
public record KnownActivityDTO(
        String code,
        String name,
        BigDecimal met) {

    public static KnownActivityDTO from(KnownActivity activity) {
        return new KnownActivityDTO(activity.name(), activity.label(), activity.met());
    }
}
