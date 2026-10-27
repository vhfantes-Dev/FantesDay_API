package com.vhfantes.fantesday.routine;

// Equipamentos disponiveis para o gerador (agrupa os valores do campo equipment do catalogo)
public enum EquipmentType {
    BARBELL,
    DUMBBELL,
    MACHINE,
    CABLE,
    BODYWEIGHT;

    // "barbell", "e-z curl bar", "body only"... -> tipo; null se o equipamento nao entra no gerador
    public static EquipmentType fromCatalog(String equipment) {
        if (equipment == null) {
            return null;
        }
        return switch (equipment.trim().toLowerCase()) {
            case "barbell", "e-z curl bar" -> BARBELL;
            case "dumbbell" -> DUMBBELL;
            case "machine" -> MACHINE;
            case "cable" -> CABLE;
            case "body only" -> BODYWEIGHT;
            default -> null;
        };
    }
}
