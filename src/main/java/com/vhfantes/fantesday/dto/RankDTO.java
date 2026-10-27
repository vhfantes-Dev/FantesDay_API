package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.strength.StrengthRank;

// Nota de forca (1-1000), rank e LP dentro do rank
public record RankDTO(
        int score,
        StrengthRank rank,
        int lp,
        StrengthRank nextRank) {

    public static RankDTO of(int score) {
        StrengthRank rank = StrengthRank.fromScore(score);
        return new RankDTO(score, rank, StrengthRank.lp(score), rank.next());
    }
}
