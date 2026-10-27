package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.WeightEntryDTO;
import com.vhfantes.fantesday.dto.WeightHistoryDTO;
import com.vhfantes.fantesday.dto.WeightRequestDTO;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.entity.WeightEntry;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.repository.WeightEntryRepository;
import com.vhfantes.fantesday.security.CurrentUser;

import lombok.RequiredArgsConstructor;

// Historico de peso corporal. O peso mais recente vira o User.bodyweightKg (usado no rank de forca).
@Service
@RequiredArgsConstructor
public class WeightService {

    public static final BigDecimal MIN_BODYWEIGHT = BigDecimal.valueOf(25);
    public static final BigDecimal MAX_BODYWEIGHT = BigDecimal.valueOf(400);
    private static final int DEFAULT_HISTORY_DAYS = 90;

    private final WeightEntryRepository weightEntryRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional
    public WeightEntryDTO add(Long userId, WeightRequestDTO dto) {
        if (dto.weightKg() == null) {
            throw new BusinessException("Peso e obrigatorio");
        }
        LocalDate date = dto.date() != null ? dto.date() : LocalDate.now();
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("Nao da para registrar peso em uma data futura");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + userId));
        return WeightEntryDTO.from(record(user, date, dto.weightKg()));
    }

    // Grava (ou substitui) o peso do dia; se for o registro mais recente, atualiza o peso do perfil
    @Transactional
    public WeightEntry record(User user, LocalDate date, BigDecimal weightKg) {
        validate(weightKg);

        WeightEntry entry = weightEntryRepository.findByUserIdAndDate(user.getId(), date).orElseGet(() -> {
            WeightEntry created = new WeightEntry();
            created.setUser(user);
            created.setDate(date);
            return created;
        });
        entry.setWeightKg(weightKg);
        entry = weightEntryRepository.saveAndFlush(entry);

        boolean isLatest = weightEntryRepository.findFirstByUserIdOrderByDateDesc(user.getId())
                .map(latest -> !date.isBefore(latest.getDate()))
                .orElse(true);
        if (isLatest) {
            user.setBodyweightKg(weightKg);
        }
        return entry;
    }

    // Padrao: ultimos 90 dias
    @Transactional(readOnly = true)
    public WeightHistoryDTO history(Long userId, LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_HISTORY_DAYS);
        if (start.isAfter(end)) {
            throw new BusinessException("A data inicial deve ser anterior a final");
        }

        List<WeightEntryDTO> entries = weightEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end)
                .stream()
                .map(WeightEntryDTO::from)
                .toList();

        BigDecimal current = userRepository.findById(userId).map(User::getBodyweightKg).orElse(null);
        BigDecimal change = entries.size() < 2
                ? null
                : entries.get(entries.size() - 1).weightKg().subtract(entries.get(0).weightKg());

        return new WeightHistoryDTO(start, end, current, change, entries);
    }

    // Se apagar o registro mais recente, o peso do perfil volta para o anterior
    @Transactional
    public void delete(Long id) {
        WeightEntry entry = weightEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de peso nao encontrado: " + id));
        User user = entry.getUser();
        currentUser.checkAccess(user.getId());

        weightEntryRepository.delete(entry);
        weightEntryRepository.flush();

        weightEntryRepository.findFirstByUserIdOrderByDateDesc(user.getId())
                .ifPresentOrElse(latest -> user.setBodyweightKg(latest.getWeightKg()), () -> user.setBodyweightKg(null));
    }

    private void validate(BigDecimal weightKg) {
        if (weightKg.compareTo(MIN_BODYWEIGHT) < 0 || weightKg.compareTo(MAX_BODYWEIGHT) > 0) {
            throw new BusinessException("Peso corporal deve estar entre " + MIN_BODYWEIGHT + " e " + MAX_BODYWEIGHT + " kg");
        }
    }
}
