package com.vhfantes.fantesday.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.MealItem;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    List<MealItem> findByMealId(Long mealId);

    boolean existsByFoodId(Long foodId);

    @Modifying
    @Query("delete from MealItem i where i.meal.id in (select m.id from Meal m where m.user.id = :userId)")
    void deleteByUserId(@Param("userId") Long userId);

    // Soma de calorias e macros de todas as refeicoes do usuario no dia
    @Query("""
            select coalesce(sum(i.calculatedCalories), 0) as calories,
                   coalesce(sum(i.calculatedProtein), 0) as protein,
                   coalesce(sum(i.calculatedCarbs), 0) as carbs,
                   coalesce(sum(i.calculatedFat), 0) as fat
            from MealItem i join i.meal m
            where m.user.id = :userId and m.date = :date
            """)
    DailyTotals sumByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    interface DailyTotals {
        Long getCalories();

        BigDecimal getProtein();

        BigDecimal getCarbs();

        BigDecimal getFat();
    }
}
