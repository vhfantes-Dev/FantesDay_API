package com.vhfantes.fantesday.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.MealItemRequestDTO;
import com.vhfantes.fantesday.dto.MealItemResponseDTO;
import com.vhfantes.fantesday.dto.MealNutritionDTO;
import com.vhfantes.fantesday.dto.MealRequestDTO;
import com.vhfantes.fantesday.dto.MealResponseDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.MealService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final CurrentUser currentUser;

    // ---------- Refeicoes ----------

    // ?date=yyyy-MM-dd filtra pelo dia (opcional)
    @GetMapping("/users/{userId}/meals")
    public List<MealResponseDTO> findByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        currentUser.checkAccess(userId);
        return date != null
                ? mealService.findByUserAndDate(userId, date)
                : mealService.findByUser(userId);
    }

    @PostMapping("/users/{userId}/meals")
    @ResponseStatus(HttpStatus.CREATED)
    public MealResponseDTO create(@PathVariable Long userId, @RequestBody MealRequestDTO dto) {
        currentUser.checkAccess(userId);
        return mealService.create(userId, dto);
    }

    @GetMapping("/meals/{id}")
    public MealResponseDTO findById(@PathVariable Long id) {
        return mealService.findById(id);
    }

    @PatchMapping("/meals/{id}")
    public MealResponseDTO update(@PathVariable Long id, @RequestBody MealRequestDTO dto) {
        return mealService.update(id, dto);
    }

    @DeleteMapping("/meals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        mealService.delete(id);
    }

    // Soma de calorias e macros da refeicao
    @GetMapping("/meals/{id}/nutrition")
    public MealNutritionDTO getNutritionTotals(@PathVariable Long id) {
        return mealService.getNutritionTotals(id);
    }

    // ---------- Itens da refeicao ----------

    @GetMapping("/meals/{mealId}/items")
    public List<MealItemResponseDTO> findItems(@PathVariable Long mealId) {
        return mealService.findItems(mealId);
    }

    @PostMapping("/meals/{mealId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MealItemResponseDTO addItem(@PathVariable Long mealId, @RequestBody MealItemRequestDTO dto) {
        return mealService.addItem(mealId, dto);
    }

    @PatchMapping("/meal-items/{itemId}")
    public MealItemResponseDTO updateItem(@PathVariable Long itemId, @RequestBody MealItemRequestDTO dto) {
        return mealService.updateItem(itemId, dto);
    }

    @DeleteMapping("/meal-items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long itemId) {
        mealService.removeItem(itemId);
    }
}
