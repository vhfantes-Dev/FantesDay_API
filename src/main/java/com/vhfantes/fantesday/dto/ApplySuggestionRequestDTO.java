package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.body.WeightGoal;

// goal: LOSE (perder peso), MAINTAIN (manter) ou GAIN (ganhar)
public record ApplySuggestionRequestDTO(
        WeightGoal goal) {
}
