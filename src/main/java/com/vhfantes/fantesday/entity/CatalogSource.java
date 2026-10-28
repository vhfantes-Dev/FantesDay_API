package com.vhfantes.fantesday.entity;

// Origem de um item de catalogo (alimento ou exercicio)
public enum CatalogSource {
    TACO,
    OPEN_FOOD_FACTS,
    FREE_EXERCISE_DB,
    WGER,
    CUSTOM,
    // Base complementar do app: pizzas, fast food, lanches, doces, suplementos e bebidas (resources/seed/extra-foods.json)
    FANTESDAY
}
