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

    // Busca pelo nome em portugues ou em ingles
    @Query("select e from Exercise e where lower(e.name) like lower(concat('%', :name, '%'))"
            + " or lower(e.nameEn) like lower(concat('%', :name, '%'))")
    Page<Exercise> searchByName(@Param("name") String name, Pageable pageable);

    Page<Exercise> findByMuscleGroupIgnoreCase(String muscleGroup, Pageable pageable);

    @Query("select e from Exercise e where lower(e.muscleGroup) = lower(:muscleGroup) and"
            + " (lower(e.name) like lower(concat('%', :name, '%')) or lower(e.nameEn) like lower(concat('%', :name, '%')))")
    Page<Exercise> searchByNameAndMuscleGroup(@Param("name") String name, @Param("muscleGroup") String muscleGroup,
            Pageable pageable);

    long countBySource(CatalogSource source);

    List<Exercise> findByExternalIdIn(Collection<String> externalIds);

    List<Exercise> findBySourceIn(Collection<CatalogSource> sources);

    List<Exercise> findBySourceInAndCategoryOrderByNameAsc(Collection<CatalogSource> sources, String category);

    // Ao excluir uma conta, os exercicios que ela cadastrou continuam no catalogo, sem dono
    @Modifying
    @Query("update Exercise e set e.createdBy = null where e.createdBy.id = :userId")
    void clearCreatedBy(@Param("userId") Long userId);
}
