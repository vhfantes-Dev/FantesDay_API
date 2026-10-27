package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.Workout;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByUserId(Long userId);

    // Dias em que o usuario treinou, em ordem (usado no streak)
    @Query("select distinct w.date from Workout w where w.user.id = :userId and w.date is not null order by w.date")
    List<LocalDate> findWorkoutDates(@Param("userId") Long userId);
}
