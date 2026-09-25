package com.vhfantes.fantesday.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ProgressResponseDTO;
import com.vhfantes.fantesday.entity.Progress;
import com.vhfantes.fantesday.repository.ProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgressService {

    // Regra de nivel: a cada 100 XP o usuario sobe 1 nivel (ajuste como quiser)
    private static final int XP_PER_LEVEL = 100;

    private final ProgressRepository progressRepository;
    private final UserService userService;

    @Transactional
    public ProgressResponseDTO findByUserId(Long userId) {
        return ProgressResponseDTO.from(getOrCreate(userId));
    }

    // Soma (ou subtrai, se negativo) XP e recalcula o nivel
    @Transactional
    public ProgressResponseDTO addXp(Long userId, int xp) {
        Progress progress = getOrCreate(userId);
        int total = Math.max(0, progress.getTotalXp() + xp);
        progress.setTotalXp(total);
        progress.setLevel(total / XP_PER_LEVEL + 1);
        return ProgressResponseDTO.from(progress);
    }

    @Transactional
    public ProgressResponseDTO updateCategory(Long userId, String category) {
        Progress progress = getOrCreate(userId);
        progress.setCategory(category);
        return ProgressResponseDTO.from(progress);
    }

    // Usuarios criados antes do UserService.create nao tem progresso: cria na hora
    private Progress getOrCreate(Long userId) {
        return progressRepository.findById(userId).orElseGet(() -> {
            Progress progress = new Progress();
            progress.setUser(userService.getUserOrThrow(userId));
            return progressRepository.save(progress);
        });
    }
}
