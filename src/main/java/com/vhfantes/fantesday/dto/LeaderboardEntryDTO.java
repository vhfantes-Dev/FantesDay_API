package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.strength.StrengthRank;

public record LeaderboardEntryDTO(
        int position,
        Long userId,
        String name,
        int score,
        StrengthRank rank) {
}
