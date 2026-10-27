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
import com.vhfantes.fantesday.dto.ExerciseRequestDTO;
import com.vhfantes.fantesday.dto.ExerciseResponseDTO;
import com.vhfantes.fantesday.dto.PageResponseDTO;
import com.vhfantes.fantesday.service.ExerciseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final Paging paging;

    // Paginado (?page=0&size=20, ordenado por nome); ?name=press e ?muscleGroup=Peito filtram (podem ser combinados)
    @GetMapping
    public PageResponseDTO<ExerciseResponseDTO> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return exerciseService.search(name, muscleGroup, paging.of(page, size, Sort.by("name")));
    }

    // Exercicios que tem rank de forca
    @GetMapping("/ranked")
    public List<ExerciseResponseDTO> findRanked() {
        return exerciseService.findRanked();
    }

    @GetMapping("/{id}")
    public ExerciseResponseDTO findById(@PathVariable Long id) {
        return exerciseService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponseDTO create(@RequestBody ExerciseRequestDTO dto) {
        return exerciseService.create(dto);
    }

    @PatchMapping("/{id}")
    public ExerciseResponseDTO update(@PathVariable Long id, @RequestBody ExerciseRequestDTO dto) {
        return exerciseService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        exerciseService.delete(id);
    }
}
