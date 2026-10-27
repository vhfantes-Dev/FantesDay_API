package com.vhfantes.fantesday.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByNameContainingIgnoreCase(String name);

    Optional<Food> findBySourceAndExternalId(CatalogSource source, String externalId);

    long countBySource(CatalogSource source);
}
