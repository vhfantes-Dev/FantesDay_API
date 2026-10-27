package com.vhfantes.fantesday.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Page<Exercise> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Exercise> findByMuscleGroupIgnoreCase(String muscleGroup, Pageable pageable);

    Page<Exercise> findByNameContainingIgnoreCaseAndMuscleGroupIgnoreCase(String name, String muscleGroup, Pageable pageable);

    long countBySource(CatalogSource source);

    List<Exercise> findByExternalIdIn(Collection<String> externalIds);

    List<Exercise> findBySourceAndCategoryOrderByNameAsc(CatalogSource source, String category);

    // Ao excluir uma conta, os exercicios que ela cadastrou continuam no catalogo, sem dono
    @Modifying
    @Query("update Exercise e set e.createdBy = null where e.createdBy.id = :userId")
    void clearCreatedBy(@Param("userId") Long userId);
}
