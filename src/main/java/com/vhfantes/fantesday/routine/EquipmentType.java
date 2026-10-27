package com.vhfantes.fantesday.routine;

// Equipamentos disponiveis para o gerador (agrupa os valores do campo equipment do catalogo)
public enum EquipmentType {
    BARBELL,
    DUMBBELL,
    MACHINE,
    CABLE,
    BODYWEIGHT;

    // Valor do campo equipment do catalogo ("Barra", "Halteres", "Polia"...) -> tipo;
    // null se o equipamento nao entra no gerador. Aceita tambem os nomes antigos em ingles.
    public static EquipmentType fromCatalog(String equipment) {
        if (equipment == null) {
            return null;
        }
        return switch (equipment.trim().toLowerCase()) {
            case "barra", "barra w", "barbell", "e-z curl bar" -> BARBELL;
            case "halteres", "dumbbell" -> DUMBBELL;
            case "máquina", "maquina", "machine" -> MACHINE;
            case "polia", "cable" -> CABLE;
            case "peso corporal", "barra fixa", "body only" -> BODYWEIGHT;
            default -> null;
        };
    }
}
