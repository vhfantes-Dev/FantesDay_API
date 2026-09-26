package com.vhfantes.fantesday.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.CategoryRequestDTO;
import com.vhfantes.fantesday.dto.ProgressResponseDTO;
import com.vhfantes.fantesday.dto.XpRequestDTO;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.service.ProgressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public ProgressResponseDTO findByUser(@PathVariable Long userId) {
        return progressService.findByUserId(userId);
    }

    @PostMapping("/xp")
    public ProgressResponseDTO addXp(@PathVariable Long userId, @RequestBody XpRequestDTO dto) {
        if (dto.xp() == null) {
            throw new BusinessException("XP e obrigatorio");
        }
        return progressService.addXp(userId, dto.xp());
    }

    @PatchMapping("/category")
    public ProgressResponseDTO updateCategory(@PathVariable Long userId, @RequestBody CategoryRequestDTO dto) {
        return progressService.updateCategory(userId, dto.category());
    }
}
