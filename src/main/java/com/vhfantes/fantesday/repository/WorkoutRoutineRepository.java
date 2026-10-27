package com.vhfantes.fantesday.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.WorkoutRoutine;

public interface WorkoutRoutineRepository extends JpaRepository<WorkoutRoutine, Long> {

    List<WorkoutRoutine> findByUserIdOrderByNameAsc(Long userId);

    @Modifying
    @Query("delete from WorkoutRoutine r where r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
