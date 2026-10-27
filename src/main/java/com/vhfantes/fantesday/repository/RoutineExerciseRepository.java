package com.vhfantes.fantesday.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.RoutineExercise;

public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {

    List<RoutineExercise> findByRoutineIdOrderByPositionAsc(Long routineId);

    List<RoutineExercise> findByRoutineIdInOrderByPositionAsc(Collection<Long> routineIds);

    boolean existsByExerciseId(Long exerciseId);

    @Modifying
    @Query("delete from RoutineExercise e where e.routine.id in (select r.id from WorkoutRoutine r where r.user.id = :userId)")
    void deleteByUserId(@Param("userId") Long userId);
}
