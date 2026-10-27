package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.MealItemRequestDTO;
import com.vhfantes.fantesday.dto.MealItemResponseDTO;
import com.vhfantes.fantesday.dto.MealNutritionDTO;
import com.vhfantes.fantesday.dto.MealRequestDTO;
import com.vhfantes.fantesday.dto.MealResponseDTO;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.entity.Meal;
import com.vhfantes.fantesday.entity.MealItem;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.food.FoodUnits;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.MealItemRepository;
import com.vhfantes.fantesday.repository.MealRepository;
import com.vhfantes.fantesday.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final UserService userService;
    private final FoodService foodService;
    private final CurrentUser currentUser;

    // ---------- Refeicoes ----------

    @Transactional(readOnly = true)
    public List<MealResponseDTO> findByUser(Long userId) {
        return mealRepository.findByUserId(userId).stream()
                .map(MealResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MealResponseDTO> findByUserAndDate(Long userId, LocalDate date) {
        return mealRepository.findByUserIdAndDate(userId, date).stream()
                .map(MealResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MealResponseDTO findById(Long id) {
        return MealResponseDTO.from(getMealOrThrow(id));
    }

    @Transactional
    public MealResponseDTO create(Long userId, MealRequestDTO dto) {
        Meal meal = new Meal();
        meal.setUser(userService.getUserOrThrow(userId));
        meal.setDate(dto.date() != null ? dto.date() : LocalDate.now());
        meal.setName(dto.name() != null && !dto.name().isBlank()
                ? dto.name().trim()
                : "Refeição " + (mealRepository.countByUserIdAndDate(userId, meal.getDate()) + 1));

        return MealResponseDTO.from(mealRepository.save(meal));
    }

    @Transactional
    public MealResponseDTO update(Long id, MealRequestDTO dto) {
        Meal meal = getMealOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            meal.setName(dto.name().trim());
        }
        if (dto.date() != null) {
            meal.setDate(dto.date());
        }

        return MealResponseDTO.from(meal);
    }

    // Remove os itens da refeicao e depois a refeicao
    @Transactional
    public void delete(Long id) {
        Meal meal = getMealOrThrow(id);
        mealItemRepository.deleteAll(mealItemRepository.findByMealId(id));
        mealRepository.delete(meal);
    }

    // ---------- Itens da refeicao ----------

    @Transactional(readOnly = true)
    public List<MealItemResponseDTO> findItems(Long mealId) {
        getMealOrThrow(mealId);
        return mealItemRepository.findByMealId(mealId).stream()
                .map(MealItemResponseDTO::from)
                .toList();
    }

    // Soma das calorias de todos os itens da refeicao
    @Transactional(readOnly = true)
    public int getTotalCalories(Long mealId) {
        getMealOrThrow(mealId);
        return mealItemRepository.findByMealId(mealId).stream()
                .map(MealItem::getCalculatedCalories)
                .filter(calories -> calories != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    // Soma de calorias e macronutrientes de todos os itens da refeicao
    @Transactional(readOnly = true)
    public MealNutritionDTO getNutritionTotals(Long mealId) {
        getMealOrThrow(mealId);
        List<MealItem> items = mealItemRepository.findByMealId(mealId);

        int calories = 0;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;

        for (MealItem item : items) {
            if (item.getCalculatedCalories() != null) {
                calories += item.getCalculatedCalories();
            }
            if (item.getCalculatedProtein() != null) {
                protein = protein.add(item.getCalculatedProtein());
            }
            if (item.getCalculatedCarbs() != null) {
                carbs = carbs.add(item.getCalculatedCarbs());
            }
            if (item.getCalculatedFat() != null) {
                fat = fat.add(item.getCalculatedFat());
            }
        }

        return new MealNutritionDTO(calories, protein, carbs, fat);
    }

    @Transactional
    public MealItemResponseDTO addItem(Long mealId, MealItemRequestDTO dto) {
        if (dto.foodId() == null) {
            throw new BusinessException("Alimento e obrigatorio");
        }
        if (dto.quantity() == null || dto.quantity().signum() <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }

        Food food = foodService.getFoodOrThrow(dto.foodId());

        MealItem item = new MealItem();
        item.setMeal(getMealOrThrow(mealId));
        item.setFood(food);
        item.setQuantity(dto.quantity());
        item.setUnit(FoodUnits.normalize(dto.unit(), food));
        applyNutrition(item);

        return MealItemResponseDTO.from(mealItemRepository.save(item));
    }

    // Se o alimento ou a quantidade mudar, recalcula calorias e macros
    @Transactional
    public MealItemResponseDTO updateItem(Long itemId, MealItemRequestDTO dto) {
        MealItem item = getItemOrThrow(itemId);

        if (dto.foodId() != null) {
            item.setFood(foodService.getFoodOrThrow(dto.foodId()));
        }
        if (dto.quantity() != null) {
            if (dto.quantity().signum() <= 0) {
                throw new BusinessException("Quantidade deve ser maior que zero");
            }
            item.setQuantity(dto.quantity());
        }
        if (dto.unit() != null) {
            item.setUnit(FoodUnits.normalize(dto.unit(), item.getFood()));
        }
        applyNutrition(item);

        return MealItemResponseDTO.from(item);
    }

    @Transactional
    public void removeItem(Long itemId) {
        mealItemRepository.delete(getItemOrThrow(itemId));
    }

    // Em ml, a quantidade vira gramas pela densidade do alimento antes do calculo
    private void applyNutrition(MealItem item) {
        Food food = item.getFood();
        BigDecimal quantity = FoodUnits.toGrams(item.getQuantity(), FoodUnits.normalizeLenient(item.getUnit(), food), food);

        BigDecimal calories = calculatePer100g(food.getCaloriesPer100g(), quantity, 0);
        item.setCalculatedCalories(calories != null ? calories.intValue() : null);
        item.setCalculatedProtein(calculatePer100g(food.getProteinPer100g(), quantity, 2));
        item.setCalculatedCarbs(calculatePer100g(food.getCarbsPer100g(), quantity, 2));
        item.setCalculatedFat(calculatePer100g(food.getFatPer100g(), quantity, 2));
    }

    // valor = quantidade em gramas x (valor por 100g / 100)
    static BigDecimal calculatePer100g(BigDecimal valuePer100g, BigDecimal quantity, int scale) {
        if (valuePer100g == null || quantity == null) {
            return null;
        }
        return quantity.multiply(valuePer100g)
                .divide(ONE_HUNDRED, scale, RoundingMode.HALF_UP);
    }

    // So o dono da refeicao pode acessa-la (ou acessar seus itens)
    private Meal getMealOrThrow(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refeicao nao encontrada: " + id));
        currentUser.checkAccess(meal.getUser().getId());
        return meal;
    }

    private MealItem getItemOrThrow(Long id) {
        MealItem item = mealItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item da refeicao nao encontrado: " + id));
        currentUser.checkAccess(item.getMeal().getUser().getId());
        return item;
    }
}
