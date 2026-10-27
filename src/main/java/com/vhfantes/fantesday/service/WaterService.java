package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.WaterDayDTO;
import com.vhfantes.fantesday.dto.WaterEntryDTO;
import com.vhfantes.fantesday.dto.WaterRequestDTO;
import com.vhfantes.fantesday.entity.NutritionGoal;
import com.vhfantes.fantesday.entity.WaterEntry;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.NutritionGoalRepository;
import com.vhfantes.fantesday.repository.WaterEntryRepository;
import com.vhfantes.fantesday.security.CurrentUser;

import lombok.RequiredArgsConstructor;

// Agua bebida no dia (cada copo/garrafa e um registro), comparada com a meta diaria
@Service
@RequiredArgsConstructor
public class WaterService {

    private static final int MAX_AMOUNT_ML = 5_000;

    private final WaterEntryRepository waterEntryRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final UserService userService;
    private final CurrentUser currentUser;

    @Transactional
    public WaterDayDTO add(Long userId, WaterRequestDTO dto) {
        if (dto.amountMl() == null || dto.amountMl() <= 0 || dto.amountMl() > MAX_AMOUNT_ML) {
            throw new BusinessException("Quantidade deve estar entre 1 e " + MAX_AMOUNT_ML + " ml");
        }
        LocalDate date = dto.date() != null ? dto.date() : LocalDate.now();

        WaterEntry entry = new WaterEntry();
        entry.setUser(userService.getUserOrThrow(userId));
        entry.setDate(date);
        entry.setAmountMl(dto.amountMl());
        waterEntryRepository.saveAndFlush(entry);

        return getDay(userId, date);
    }

    @Transactional(readOnly = true)
    public WaterDayDTO getDay(Long userId, LocalDate date) {
        List<WaterEntryDTO> entries = waterEntryRepository.findByUserIdAndDateOrderByCreatedAtAsc(userId, date).stream()
                .map(WaterEntryDTO::from)
                .toList();
        int total = entries.stream().mapToInt(WaterEntryDTO::amountMl).sum();

        Integer goal = nutritionGoalRepository.findById(userId).map(NutritionGoal::getDailyWaterMl).orElse(null);
        Integer percent = goal != null && goal > 0 ? (int) Math.round(total * 100.0 / goal) : null;

        return new WaterDayDTO(date, total, goal, percent, entries);
    }

    @Transactional
    public void delete(Long id) {
        WaterEntry entry = waterEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de agua nao encontrado: " + id));
        currentUser.checkAccess(entry.getUser().getId());
        waterEntryRepository.delete(entry);
    }
}
