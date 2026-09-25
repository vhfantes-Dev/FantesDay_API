package com.vhfantes.fantesday.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.MealItem;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    List<MealItem> findByMealId(Long mealId);
}
