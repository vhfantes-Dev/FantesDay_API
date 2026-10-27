package com.vhfantes.fantesday.strength;

// Os 9 ranks de forca (mesmos nomes do Liftoff). Cada rank cobre 100 pontos da nota 0-1000;
// o Olympian comeca em 800 e vai ate 1000.
public enum StrengthRank {
    WOOD,
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND,
    CHAMPION,
    TITAN,
    OLYMPIAN;

    public static final int POINTS_PER_RANK = 100;

    public static StrengthRank fromScore(int score) {
        int index = Math.max(0, Math.min(score / POINTS_PER_RANK, values().length - 1));
        return values()[index];
    }

    // LP (lift points) = pontos dentro do rank atual; sobe de rank a cada 100 LP (exceto no Olympian)
    public static int lp(int score) {
        return Math.max(0, score) - fromScore(score).ordinal() * POINTS_PER_RANK;
    }

    // Proximo rank, ou null se ja for Olympian
    public StrengthRank next() {
        return this == OLYMPIAN ? null : values()[ordinal() + 1];
    }
}
