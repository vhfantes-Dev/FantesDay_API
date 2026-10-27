package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ActivityImportRequestDTO;
import com.vhfantes.fantesday.dto.ActivityImportResultDTO;
import com.vhfantes.fantesday.dto.ActivityRequestDTO;
import com.vhfantes.fantesday.dto.ActivityResponseDTO;
import com.vhfantes.fantesday.entity.Activity;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ActivityRepository;
import com.vhfantes.fantesday.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityService {

    public static final String IMPORT_ORIGIN = "import";
    private static final int MAX_IMPORT_BATCH = 500;

    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final ProgressService progressService;
    private final CurrentUser currentUser;

    // 1 XP a cada N minutos de atividade, com limite por atividade (app.xp.*)
    @Value("${app.xp.activity-minutes-per-xp}")
    private int minutesPerXp;

    @Value("${app.xp.activity-max}")
    private int maxXpPerActivity;

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

    // Cria a atividade e soma o XP pela duracao
    @Transactional
    public ActivityResponseDTO create(Long userId, ActivityRequestDTO dto) {
        if (dto.activityType() == null || dto.activityType().isBlank()) {
            throw new BusinessException("Tipo de atividade e obrigatorio");
        }
        validateDuration(dto.durationMinutes());

        Activity activity = new Activity();
        activity.setUser(userService.getUserOrThrow(userId));
        activity.setActivityType(dto.activityType().trim());
        activity.setDurationMinutes(dto.durationMinutes());
        activity.setOrigin(dto.origin());
        activity.setImportSource(dto.importSource());
        activity.setCaloriesBurned(dto.caloriesBurned());
        activity.setDate(dto.date() != null ? dto.date() : LocalDate.now());
        activity.setXpEarned(xpFor(dto.durationMinutes()));

        activity = activityRepository.save(activity);
        progressService.addXp(userId, activity.getXpEarned());
        return ActivityResponseDTO.from(activity);
    }

    // Atualiza apenas os campos enviados; se a duracao mudar, ajusta a diferenca de XP
    @Transactional
    public ActivityResponseDTO update(Long id, ActivityRequestDTO dto) {
        Activity activity = getActivityOrThrow(id);

        if (dto.activityType() != null && !dto.activityType().isBlank()) {
            activity.setActivityType(dto.activityType().trim());
        }
        if (dto.durationMinutes() != null) {
            validateDuration(dto.durationMinutes());
            activity.setDurationMinutes(dto.durationMinutes());

            int newXp = xpFor(dto.durationMinutes());
            int difference = newXp - xpOf(activity);
            activity.setXpEarned(newXp);
            if (difference != 0) {
                progressService.addXp(activity.getUser().getId(), difference);
            }
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

    // Exclui e desconta o XP que a atividade deu
    @Transactional
    public void delete(Long id) {
        Activity activity = getActivityOrThrow(id);
        if (xpOf(activity) != 0) {
            progressService.addXp(activity.getUser().getId(), -xpOf(activity));
        }
        activityRepository.delete(activity);
    }

    // Importa atividades de outro app (Apple Saude, Google Fit...). Atividades ja importadas sao ignoradas.
    @Transactional
    public ActivityImportResultDTO importActivities(Long userId, ActivityImportRequestDTO dto) {
        if (dto.source() == null || dto.source().isBlank()) {
            throw new BusinessException("Origem (source) e obrigatoria, ex: APPLE_HEALTH");
        }
        if (dto.activities() == null || dto.activities().isEmpty()) {
            throw new BusinessException("Nenhuma atividade para importar");
        }
        if (dto.activities().size() > MAX_IMPORT_BATCH) {
            throw new BusinessException("Envie no maximo " + MAX_IMPORT_BATCH + " atividades por vez");
        }

        User user = userService.getUserOrThrow(userId);
        String source = dto.source().trim().toUpperCase();

        List<ActivityResponseDTO> imported = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();
        int skipped = 0;
        int totalXp = 0;

        for (ActivityImportRequestDTO.Item item : dto.activities()) {
            if (item.externalId() == null || item.externalId().isBlank()) {
                throw new BusinessException("Toda atividade importada precisa de externalId");
            }
            if (item.activityType() == null || item.activityType().isBlank()) {
                throw new BusinessException("Tipo de atividade e obrigatorio (externalId " + item.externalId() + ")");
            }
            validateDuration(item.durationMinutes());

            String externalId = item.externalId().trim();
            if (!seenInBatch.add(externalId)
                    || activityRepository.existsByUserIdAndImportSourceAndExternalId(userId, source, externalId)) {
                skipped++;
                continue;
            }

            Activity activity = new Activity();
            activity.setUser(user);
            activity.setActivityType(item.activityType().trim());
            activity.setDurationMinutes(item.durationMinutes());
            activity.setCaloriesBurned(item.caloriesBurned());
            activity.setDate(item.date() != null ? item.date() : LocalDate.now());
            activity.setOrigin(IMPORT_ORIGIN);
            activity.setImportSource(source);
            activity.setExternalId(externalId);
            activity.setXpEarned(xpFor(item.durationMinutes()));

            activity = activityRepository.save(activity);
            totalXp += activity.getXpEarned();
            imported.add(ActivityResponseDTO.from(activity));
        }

        if (totalXp != 0) {
            progressService.addXp(userId, totalXp);
        }
        return new ActivityImportResultDTO(imported.size(), skipped, totalXp, imported);
    }

    int xpFor(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0 || minutesPerXp <= 0) {
            return 0;
        }
        return Math.min(maxXpPerActivity, durationMinutes / minutesPerXp);
    }

    private int xpOf(Activity activity) {
        return activity.getXpEarned() != null ? activity.getXpEarned() : 0;
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes != null && (durationMinutes < 0 || durationMinutes > 24 * 60)) {
            throw new BusinessException("Duracao deve estar entre 0 e 1440 minutos");
        }
    }

    // So o dono da atividade pode acessa-la
    private Activity getActivityOrThrow(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade nao encontrada: " + id));
        currentUser.checkAccess(activity.getUser().getId());
        return activity;
    }
}
