package com.vhfantes.fantesday.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.FoodRequestDTO;
import com.vhfantes.fantesday.dto.FoodResponseDTO;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.FoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    @Transactional(readOnly = true)
    public List<FoodResponseDTO> findAll() {
        return foodRepository.findAll().stream()
                .map(FoodResponseDTO::from)
                .toList();
    }

    // Pesquisa por parte do nome, sem diferenciar maiusculas/minusculas
    @Transactional(readOnly = true)
    public List<FoodResponseDTO> searchByName(String name) {
        return foodRepository.findByNameContainingIgnoreCase(name).stream()
                .map(FoodResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FoodResponseDTO findById(Long id) {
        return FoodResponseDTO.from(getFoodOrThrow(id));
    }

    @Transactional
    public FoodResponseDTO create(FoodRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new BusinessException("Nome e obrigatorio");
        }

        Food food = new Food();
        food.setName(dto.name().trim());
        food.setCaloriesPer100g(dto.caloriesPer100g());
        food.setDefaultUnit(dto.defaultUnit());

        return FoodResponseDTO.from(foodRepository.save(food));
    }

    @Transactional
    public FoodResponseDTO update(Long id, FoodRequestDTO dto) {
        Food food = getFoodOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            food.setName(dto.name().trim());
        }
        if (dto.caloriesPer100g() != null) {
            food.setCaloriesPer100g(dto.caloriesPer100g());
        }
        if (dto.defaultUnit() != null) {
            food.setDefaultUnit(dto.defaultUnit());
        }

        return FoodResponseDTO.from(food);
    }

    @Transactional
    public void delete(Long id) {
        foodRepository.delete(getFoodOrThrow(id));
    }

    // Usado pelo MealService para montar os itens de uma refeicao
    @Transactional(readOnly = true)
    public Food getFoodOrThrow(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alimento nao encontrado: " + id));
    }
}
