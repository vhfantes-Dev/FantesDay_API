package com.vhfantes.fantesday.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.AuthResponseDTO;
import com.vhfantes.fantesday.dto.LoginRequestDTO;
import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Publico: cria a conta e devolve o token
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@RequestBody UserRequestDTO dto) {
        return authService.register(dto);
    }

    // Publico: troca e-mail e senha por um token
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    // Usuario dono do token
    @GetMapping("/me")
    public UserResponseDTO me() {
        return authService.me();
    }
}
