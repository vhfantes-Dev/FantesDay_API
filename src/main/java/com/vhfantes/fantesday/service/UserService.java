package com.vhfantes.fantesday.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.entity.Progress;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.ProgressRepository;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.security.PasswordPolicy;

import lombok.RequiredArgsConstructor;

// A exclusao da conta (com todos os dados) fica no AccountDeletionService
@Service
@RequiredArgsConstructor // injecao pelo construtor (gerado pelo Lombok para os campos final)
public class UserService {

    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final PasswordEncoder passwordEncoder;
    private final WeightService weightService;

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return UserResponseDTO.from(getUserOrThrow(id));
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        validateRequired(dto.name(), "Nome");
        validateRequired(dto.email(), "E-mail");
        PasswordPolicy.validate(dto.password());

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
        user.setSex(dto.sex());
        user = userRepository.save(user);

        // Todo usuario nasce com um progresso zerado (relacao 1:1)
        Progress progress = new Progress();
        progress.setUser(user);
        progressRepository.save(progress);

        // O peso informado no cadastro ja entra no historico de peso
        if (dto.bodyweightKg() != null) {
            weightService.record(user, LocalDate.now(), dto.bodyweightKg());
        }

        return UserResponseDTO.from(user);
    }

    // Atualiza apenas os campos enviados (os que vierem null sao ignorados).
    // A senha nao muda por aqui: use POST /api/auth/change-password (exige a senha atual).
    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = getUserOrThrow(id);

        if (dto.password() != null) {
            throw new BusinessException("Para trocar a senha use POST /api/auth/change-password");
        }

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

        if (dto.faceIdEnabled() != null) {
            user.setFaceIdEnabled(dto.faceIdEnabled());
        }

        // Mudar o peso pelo perfil registra o peso de hoje no historico
        if (dto.bodyweightKg() != null) {
            weightService.record(user, LocalDate.now(), dto.bodyweightKg());
        }

        if (dto.sex() != null) {
            user.setSex(dto.sex());
        }

        // Sem save(): dentro de @Transactional o Hibernate grava as alteracoes sozinho
        return UserResponseDTO.from(user);
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
}
