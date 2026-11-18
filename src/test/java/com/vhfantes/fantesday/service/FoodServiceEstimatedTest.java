package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vhfantes.fantesday.client.OpenFoodFactsClient;
import com.vhfantes.fantesday.dto.FoodResponseDTO;
import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.repository.FoodRepository;
import com.vhfantes.fantesday.repository.MealItemRepository;
import com.vhfantes.fantesday.security.CurrentUser;

// Itens da foto que o catalogo nao tem viram alimentos do usuario
@ExtendWith(MockitoExtension.class)
class FoodServiceEstimatedTest {

    private static final long USER_ID = 7L;

    @Mock
    private FoodRepository foodRepository;
    @Mock
    private MealItemRepository mealItemRepository;
    @Mock
    private OpenFoodFactsClient openFoodFactsClient;
    @Mock
    private UserService userService;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private FoodService foodService;

    @BeforeEach
    void setUp() {
        when(currentUser.id()).thenReturn(USER_ID);
    }

    @Test
    void createsUserFoodWithValuesPer100gFromThePortion() {
        User user = new User();
        user.setId(USER_ID);
        when(foodRepository.findFirstByCreatedByIdAndSourceAndNameIgnoreCase(USER_ID, CatalogSource.CUSTOM, "Pizza de calabresa"))
                .thenReturn(Optional.empty());
        when(userService.getUserOrThrow(USER_ID)).thenReturn(user);
        when(foodRepository.save(any(Food.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1 fatia de 120 g com 330 kcal -> 275 kcal por 100 g
        FoodResponseDTO food = foodService.findOrCreateEstimated(" Pizza de calabresa ", new BigDecimal("120"),
                new BigDecimal("330"), new BigDecimal("13.2"), new BigDecimal("33"), new BigDecimal("16.8"));

        assertThat(food.name()).isEqualTo("Pizza de calabresa");
        assertThat(food.caloriesPer100g()).isEqualByComparingTo("275");
        assertThat(food.proteinPer100g()).isEqualByComparingTo("11");
        assertThat(food.carbsPer100g()).isEqualByComparingTo("27.5");
        assertThat(food.fatPer100g()).isEqualByComparingTo("14");
        assertThat(food.servingGrams()).isEqualByComparingTo("120");
        assertThat(food.servingDescription()).isEqualTo(FoodService.ESTIMATED_SERVING_DESCRIPTION);
        assertThat(food.source()).isEqualTo(CatalogSource.CUSTOM);
        assertThat(food.createdById()).isEqualTo(USER_ID);
    }

    @Test
    void reusesTheUserFoodWithTheSameName() {
        Food existing = new Food();
        existing.setId(99L);
        existing.setName("Pizza de calabresa");
        when(foodRepository.findFirstByCreatedByIdAndSourceAndNameIgnoreCase(USER_ID, CatalogSource.CUSTOM, "Pizza de calabresa"))
                .thenReturn(Optional.of(existing));

        FoodResponseDTO food = foodService.findOrCreateEstimated("Pizza de calabresa", new BigDecimal("110"),
                new BigDecimal("300"), BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);

        assertThat(food.id()).isEqualTo(99L);
        verify(foodRepository, never()).save(any());
    }
}
