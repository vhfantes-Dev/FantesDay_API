package com.vhfantes.fantesday.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

    Page<Food> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Food> findBySourceAndExternalId(CatalogSource source, String externalId);

    long countBySource(CatalogSource source);

    List<Food> findBySource(CatalogSource source);

    List<Food> findBySourceIn(Collection<CatalogSource> sources);

    // Alimento que o usuario ja tem com esse nome (ex: um item da foto cadastrado antes)
    Optional<Food> findFirstByCreatedByIdAndSourceAndNameIgnoreCase(Long userId, CatalogSource source, String name);

    // Ao excluir uma conta, os alimentos que ela cadastrou continuam no catalogo, sem dono
    @Modifying
    @Query("update Food f set f.createdBy = null where f.createdBy.id = :userId")
    void clearCreatedBy(@Param("userId") Long userId);
}
