package com.vhfantes.fantesday.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.AuthResponseDTO;
import com.vhfantes.fantesday.dto.LoginRequestDTO;
import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.UnauthorizedException;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.security.TokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CurrentUser currentUser;

    // Cria a conta e ja devolve o token (o usuario entra logado)
    @Transactional
    public AuthResponseDTO register(UserRequestDTO dto) {
        UserResponseDTO created = userService.create(dto);
        return authenticated(userService.getUserOrThrow(created.id()));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        if (dto.email() == null || dto.email().isBlank() || dto.password() == null || dto.password().isBlank()) {
            throw new BusinessException("E-mail e senha sao obrigatorios");
        }

        // Mesma mensagem para e-mail inexistente e senha errada, para nao revelar quais e-mails existem
        User user = userRepository.findByEmail(dto.email().trim().toLowerCase())
                .filter(found -> passwordEncoder.matches(dto.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha invalidos"));

        return authenticated(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO me() {
        return userService.findById(currentUser.id());
    }

    private AuthResponseDTO authenticated(User user) {
        TokenService.IssuedToken token = tokenService.generate(user);
        return new AuthResponseDTO(token.value(), "Bearer", token.expiresAt(), UserResponseDTO.from(user));
    }
}
