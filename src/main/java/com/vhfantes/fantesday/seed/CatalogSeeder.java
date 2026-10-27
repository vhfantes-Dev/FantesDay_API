package com.vhfantes.fantesday.seed;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.repository.FoodRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// Importa os catalogos de alimentos (TACO) e exercicios (free-exercise-db) na primeira execucao.
// Se a fonte ja tiver registros no banco, a importacao e pulada.
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class CatalogSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CatalogSeeder.class);

    private static final String TACO_FILE = "seed/taco.json";
    private static final String EXERCISES_FILE = "seed/exercises.json";
    private static final String EXERCISE_IMAGE_BASE_URL = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/";

    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;
    private final JsonMapper jsonMapper;

    public CatalogSeeder(FoodRepository foodRepository, ExerciseRepository exerciseRepository, JsonMapper jsonMapper) {
        this.foodRepository = foodRepository;
        this.exerciseRepository = exerciseRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void run(String... args) {
        try {
            seedFoods();
            seedExercises();
        } catch (Exception e) {
            logger.error("Falha ao importar catalogos: {}", e.getMessage(), e);
        }
    }

    private void seedFoods() throws IOException {
        if (foodRepository.countBySource(CatalogSource.TACO) > 0) {
            logger.info("Alimentos da TACO ja importados, pulando");
            return;
        }

        List<Food> foods = read(TACO_FILE, new TypeReference<List<TacoFoodSeed>>() {
        }).stream()
                .map(this::toFood)
                .toList();

        foodRepository.saveAll(foods);
        logger.info("Importados {} alimentos da TACO", foods.size());
    }

    private void seedExercises() throws IOException {
        if (exerciseRepository.countBySource(CatalogSource.FREE_EXERCISE_DB) > 0) {
            logger.info("Exercicios do free-exercise-db ja importados, pulando");
            return;
        }

        List<Exercise> exercises = read(EXERCISES_FILE, new TypeReference<List<FreeExerciseSeed>>() {
        }).stream()
                .map(this::toExercise)
                .toList();

        exerciseRepository.saveAll(exercises);
        logger.info("Importados {} exercicios do free-exercise-db", exercises.size());
    }

    private <T> T read(String path, TypeReference<T> type) throws IOException {
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            return jsonMapper.readValue(input, type);
        }
    }

    private Food toFood(TacoFoodSeed seed) {
        Food food = new Food();
        food.setName(seed.description());
        food.setCaloriesPer100g(seed.energyKcal());
        food.setProteinPer100g(seed.protein());
        food.setCarbsPer100g(seed.carbohydrate());
        food.setFatPer100g(seed.lipid());
        food.setFiberPer100g(seed.fiber());
        food.setDefaultUnit("g");
        food.setSource(CatalogSource.TACO);
        food.setExternalId(String.valueOf(seed.id()));
        return food;
    }

    private Exercise toExercise(FreeExerciseSeed seed) {
        Exercise exercise = new Exercise();
        exercise.setName(seed.name());
        exercise.setMuscleGroup(seed.primaryMuscles() == null || seed.primaryMuscles().isEmpty()
                ? null
                : MuscleGroupTranslator.translate(seed.primaryMuscles().get(0)));
        exercise.setSecondaryMuscles(MuscleGroupTranslator.translateAll(seed.secondaryMuscles()));
        exercise.setEquipment(seed.equipment());
        exercise.setLevel(seed.level());
        exercise.setCategory(seed.category());
        exercise.setInstructions(seed.instructions() == null ? null : String.join("\n", seed.instructions()));
        exercise.setImageUrl(seed.images() == null || seed.images().isEmpty()
                ? null
                : EXERCISE_IMAGE_BASE_URL + seed.images().get(0));
        exercise.setSource(CatalogSource.FREE_EXERCISE_DB);
        exercise.setExternalId(seed.id());
        return exercise;
    }
}
