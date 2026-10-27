package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.Meal;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByUserId(Long userId);

    List<Meal> findByUserIdAndDate(Long userId, LocalDate date);

    long countByUserIdAndDate(Long userId, LocalDate date);
}
