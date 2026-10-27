package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.entity.Progress;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.NutritionGoalRepository;
import com.vhfantes.fantesday.repository.ProgressRepository;
import com.vhfantes.fantesday.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // injecao pelo construtor (gerado pelo Lombok para os campos final)
public class UserService {

    private static final BigDecimal MIN_BODYWEIGHT = BigDecimal.valueOf(25);
    private static final BigDecimal MAX_BODYWEIGHT = BigDecimal.valueOf(400);

    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return UserResponseDTO.from(getUserOrThrow(id));
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        validateRequired(dto.name(), "Nome");
        validateRequired(dto.email(), "E-mail");
        validateRequired(dto.password(), "Senha");

        String email = normalizeEmail(dto.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail ja cadastrado");
        }

        User user = new User();
        user.setName(dto.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        if (dto.faceIdEnabled() != null) {
            user.setFaceIdEnabled(dto.faceIdEnabled());
        }
        if (dto.bodyweightKg() != null) {
            user.setBodyweightKg(validateBodyweight(dto.bodyweightKg()));
        }
        user.setSex(dto.sex());
        user = userRepository.save(user);

        // Todo usuario nasce com um progresso zerado (relacao 1:1)
        Progress progress = new Progress();
        progress.setUser(user);
        progressRepository.save(progress);

        return UserResponseDTO.from(user);
    }

    // Atualiza apenas os campos enviados (os que vierem null sao ignorados)
    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = getUserOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name().trim());
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            String email = normalizeEmail(dto.email());
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new BusinessException("E-mail ja cadastrado");
            }
            user.setEmail(email);
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        if (dto.faceIdEnabled() != null) {
            user.setFaceIdEnabled(dto.faceIdEnabled());
        }

        if (dto.bodyweightKg() != null) {
            user.setBodyweightKg(validateBodyweight(dto.bodyweightKg()));
        }

        if (dto.sex() != null) {
            user.setSex(dto.sex());
        }

        // Sem save(): dentro de @Transactional o Hibernate grava as alteracoes sozinho
        return UserResponseDTO.from(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUserOrThrow(id);
        progressRepository.deleteById(user.getId());
        nutritionGoalRepository.deleteById(user.getId());
        userRepository.delete(user);
    }

    // Metodo auxiliar para outros services (ex.: TaskService precisa do User para criar uma Task)
    @Transactional(readOnly = true)
    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + " e obrigatorio");
        }
    }

    private BigDecimal validateBodyweight(BigDecimal bodyweightKg) {
        if (bodyweightKg.compareTo(MIN_BODYWEIGHT) < 0 || bodyweightKg.compareTo(MAX_BODYWEIGHT) > 0) {
            throw new BusinessException("Peso corporal deve estar entre " + MIN_BODYWEIGHT + " e " + MAX_BODYWEIGHT + " kg");
        }
        return bodyweightKg;
    }
}
