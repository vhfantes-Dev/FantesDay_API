package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.Meal;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByUserId(Long userId);

    List<Meal> findByUserIdAndDate(Long userId, LocalDate date);

    long countByUserIdAndDate(Long userId, LocalDate date);

    @Modifying
    @Query("delete from Meal m where m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
