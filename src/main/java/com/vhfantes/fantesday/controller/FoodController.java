package com.vhfantes.fantesday.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
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

import com.vhfantes.fantesday.config.Paging;
import com.vhfantes.fantesday.dto.ExternalFoodDTO;
import com.vhfantes.fantesday.dto.PageResponseDTO;
import com.vhfantes.fantesday.dto.FoodRequestDTO;
import com.vhfantes.fantesday.dto.FoodResponseDTO;
import com.vhfantes.fantesday.service.FoodService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
    private final Paging paging;

    // Paginado (?page=0&size=20, ordenado por nome); ?name=arroz pesquisa por parte do nome
    @GetMapping
    public PageResponseDTO<FoodResponseDTO> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return foodService.search(name, paging.of(page, size, Sort.by("name")));
    }

    @GetMapping("/{id}")
    public FoodResponseDTO findById(@PathVariable Long id) {
        return foodService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponseDTO create(@RequestBody FoodRequestDTO dto) {
        return foodService.create(dto);
    }

    @PatchMapping("/{id}")
    public FoodResponseDTO update(@PathVariable Long id, @RequestBody FoodRequestDTO dto) {
        return foodService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        foodService.delete(id);
    }

    // Busca produtos no Open Food Facts sem salvar no banco
    @GetMapping("/external")
    public List<ExternalFoodDTO> searchExternal(@RequestParam String q) {
        return foodService.searchExternal(q);
    }

    // Devolve o produto do banco ou importa do Open Food Facts pelo codigo de barras
    @PostMapping("/barcode/{barcode}")
    public FoodResponseDTO findOrImportByBarcode(@PathVariable String barcode) {
        return foodService.findOrImportByBarcode(barcode);
    }
}
