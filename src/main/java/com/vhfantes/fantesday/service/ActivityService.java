package com.vhfantes.fantesday.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ActivityRequestDTO;
import com.vhfantes.fantesday.dto.ActivityResponseDTO;
import com.vhfantes.fantesday.entity.Activity;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ActivityRepository;
import com.vhfantes.fantesday.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> findByUser(Long userId) {
        return activityRepository.findByUserId(userId).stream()
                .map(ActivityResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponseDTO findById(Long id) {
        return ActivityResponseDTO.from(getActivityOrThrow(id));
    }

    @Transactional
    public ActivityResponseDTO create(Long userId, ActivityRequestDTO dto) {
        if (dto.activityType() == null || dto.activityType().isBlank()) {
            throw new BusinessException("Tipo de atividade e obrigatorio");
        }

        Activity activity = new Activity();
        activity.setUser(userService.getUserOrThrow(userId));
        activity.setActivityType(dto.activityType().trim());
        activity.setDurationMinutes(dto.durationMinutes());
        activity.setOrigin(dto.origin());
        activity.setImportSource(dto.importSource());
        activity.setCaloriesBurned(dto.caloriesBurned());
        activity.setDate(dto.date());

        return ActivityResponseDTO.from(activityRepository.save(activity));
    }

    // Atualiza apenas os campos enviados (os que vierem null sao ignorados)
    @Transactional
    public ActivityResponseDTO update(Long id, ActivityRequestDTO dto) {
        Activity activity = getActivityOrThrow(id);

        if (dto.activityType() != null && !dto.activityType().isBlank()) {
            activity.setActivityType(dto.activityType().trim());
        }
        if (dto.durationMinutes() != null) {
            activity.setDurationMinutes(dto.durationMinutes());
        }
        if (dto.origin() != null) {
            activity.setOrigin(dto.origin());
        }
        if (dto.importSource() != null) {
            activity.setImportSource(dto.importSource());
        }
        if (dto.caloriesBurned() != null) {
            activity.setCaloriesBurned(dto.caloriesBurned());
        }
        if (dto.date() != null) {
            activity.setDate(dto.date());
        }

        return ActivityResponseDTO.from(activity);
    }

    @Transactional
    public void delete(Long id) {
        activityRepository.delete(getActivityOrThrow(id));
    }

    // So o dono da atividade pode acessa-la
    private Activity getActivityOrThrow(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade nao encontrada: " + id));
        currentUser.checkAccess(activity.getUser().getId());
        return activity;
    }
}
