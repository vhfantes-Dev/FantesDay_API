package com.vhfantes.fantesday.seed;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.food.FoodMatcher;
import com.vhfantes.fantesday.food.FoodUnits;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.repository.FoodRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// Importa o catalogo de alimentos (TACO) na primeira execucao e mantem sincronizados com os arquivos:
// - a base complementar de alimentos (pizzas, fast food, doces, suplementos...) de resources/seed/extra-foods.json;
// - os exercicios (free-exercise-db traduzido + wger em portugues) de resources/seed/exercises-pt.json.
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class CatalogSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CatalogSeeder.class);

    private static final String TACO_FILE = "seed/taco.json";
    private static final String EXTRA_FOODS_FILE = "seed/extra-foods.json";
    private static final String EXERCISES_FILE = "seed/exercises-pt.json";

    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;
    private final JsonMapper jsonMapper;
    private final FoodMatcher foodMatcher;

    public CatalogSeeder(FoodRepository foodRepository, ExerciseRepository exerciseRepository, JsonMapper jsonMapper,
            FoodMatcher foodMatcher) {
        this.foodRepository = foodRepository;
        this.exerciseRepository = exerciseRepository;
        this.jsonMapper = jsonMapper;
        this.foodMatcher = foodMatcher;
    }

    @Override
    public void run(String... args) {
        try {
            seedFoods();
            seedExtraFoods();
            foodMatcher.reload();
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

    // Insere os alimentos da base complementar que faltam e atualiza os que mudaram no arquivo.
    // Alimentos que sairam do arquivo continuam no banco (podem estar em refeicoes ja registradas).
    private void seedExtraFoods() throws IOException {
        List<ExtraFoodSeed> seeds = read(EXTRA_FOODS_FILE, new TypeReference<List<ExtraFoodSeed>>() {
        });

        Map<String, Food> existing = new HashMap<>();
        for (Food food : foodRepository.findBySource(CatalogSource.FANTESDAY)) {
            existing.put(food.getExternalId(), food);
        }

        List<Food> changed = new ArrayList<>();
        int created = 0;
        for (ExtraFoodSeed seed : seeds) {
            Food food = existing.get(seed.externalId());
            if (food == null) {
                food = new Food();
                created++;
            } else if (matches(food, seed)) {
                continue;
            }
            apply(food, seed);
            changed.add(food);
        }

        if (changed.isEmpty()) {
            logger.info("Base complementar de alimentos ja atualizada ({} alimentos)", seeds.size());
            return;
        }
        foodRepository.saveAll(changed);
        logger.info("Base complementar de alimentos: {} novos e {} atualizados", created, changed.size() - created);
    }

    private void apply(Food food, ExtraFoodSeed seed) {
        boolean liquid = Boolean.TRUE.equals(seed.liquid());
        food.setSource(CatalogSource.FANTESDAY);
        food.setExternalId(seed.externalId());
        food.setName(seed.name());
        food.setCaloriesPer100g(seed.energyKcal());
        food.setProteinPer100g(seed.protein());
        food.setCarbsPer100g(seed.carbohydrate());
        food.setFatPer100g(seed.lipid());
        food.setFiberPer100g(seed.fiber());
        food.setLiquid(liquid);
        food.setDensityGPerMl(seed.densityGPerMl());
        food.setDefaultUnit(liquid ? FoodUnits.MILLILITERS : FoodUnits.GRAMS);
        food.setServingGrams(seed.servingGrams());
        food.setServingDescription(seed.servingDescription());
    }

    // BigDecimal: 12.50 (banco) e 12.5 (arquivo) sao o mesmo valor
    private boolean matches(Food food, ExtraFoodSeed seed) {
        return Objects.equals(food.getName(), seed.name())
                && sameNumber(food.getCaloriesPer100g(), seed.energyKcal())
                && sameNumber(food.getProteinPer100g(), seed.protein())
                && sameNumber(food.getCarbsPer100g(), seed.carbohydrate())
                && sameNumber(food.getFatPer100g(), seed.lipid())
                && sameNumber(food.getFiberPer100g(), seed.fiber())
                && Objects.equals(food.getLiquid(), Boolean.TRUE.equals(seed.liquid()))
                && sameNumber(food.getDensityGPerMl(), seed.densityGPerMl())
                && sameNumber(food.getServingGrams(), seed.servingGrams())
                && Objects.equals(food.getServingDescription(), seed.servingDescription());
    }

    private static boolean sameNumber(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }

    // Insere os exercicios que faltam e atualiza os que mudaram no arquivo (nome, descricao, musculos...).
    // Assim um banco que ja tinha o catalogo antigo (em ingles) recebe as traducoes sem perder treinos e historico.
    private void seedExercises() throws IOException {
        List<ExerciseSeed> seeds = read(EXERCISES_FILE, new TypeReference<List<ExerciseSeed>>() {
        });

        Map<String, Exercise> existing = new HashMap<>();
        for (Exercise exercise : exerciseRepository.findBySourceIn(List.of(CatalogSource.FREE_EXERCISE_DB, CatalogSource.WGER))) {
            existing.put(exercise.getExternalId(), exercise);
        }

        List<Exercise> changed = new ArrayList<>();
        int created = 0;
        for (ExerciseSeed seed : seeds) {
            Exercise exercise = existing.get(seed.externalId());
            if (exercise == null) {
                exercise = new Exercise();
                created++;
            } else if (matches(exercise, seed)) {
                continue;
            }
            apply(exercise, seed);
            changed.add(exercise);
        }

        if (changed.isEmpty()) {
            logger.info("Catalogo de exercicios ja atualizado ({} exercicios)", seeds.size());
            return;
        }
        exerciseRepository.saveAll(changed);
        logger.info("Catalogo de exercicios: {} novos e {} atualizados", created, changed.size() - created);
    }

    private void apply(Exercise exercise, ExerciseSeed seed) {
        exercise.setSource(seed.source());
        exercise.setExternalId(seed.externalId());
        exercise.setName(seed.name());
        exercise.setNameEn(seed.nameEn());
        exercise.setInstructions(seed.description());
        exercise.setInstructionsLanguage(seed.descriptionLanguage());
        exercise.setMuscleGroup(seed.muscleGroup());
        exercise.setSecondaryMuscles(seed.secondaryMuscles());
        exercise.setEquipment(seed.equipment());
        exercise.setLevel(seed.level());
        exercise.setCategory(seed.category());
        exercise.setImageUrl(seed.imageUrl());
    }

    private boolean matches(Exercise exercise, ExerciseSeed seed) {
        return Objects.equals(exercise.getName(), seed.name())
                && Objects.equals(exercise.getNameEn(), seed.nameEn())
                && Objects.equals(exercise.getInstructions(), seed.description())
                && Objects.equals(exercise.getInstructionsLanguage(), seed.descriptionLanguage())
                && Objects.equals(exercise.getMuscleGroup(), seed.muscleGroup())
                && Objects.equals(exercise.getSecondaryMuscles(), seed.secondaryMuscles())
                && Objects.equals(exercise.getEquipment(), seed.equipment())
                && Objects.equals(exercise.getLevel(), seed.level())
                && Objects.equals(exercise.getCategory(), seed.category())
                && Objects.equals(exercise.getImageUrl(), seed.imageUrl());
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
        boolean liquid = Boolean.TRUE.equals(seed.liquid());
        food.setLiquid(liquid);
        food.setDensityGPerMl(seed.densityGPerMl());
        food.setDefaultUnit(liquid ? FoodUnits.MILLILITERS : FoodUnits.GRAMS);
        food.setSource(CatalogSource.TACO);
        food.setExternalId(String.valueOf(seed.id()));
        return food;
    }
}
