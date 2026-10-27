package com.vhfantes.fantesday.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.AuthResponseDTO;
import com.vhfantes.fantesday.dto.ChangePasswordRequestDTO;
import com.vhfantes.fantesday.dto.ForgotPasswordRequestDTO;
import com.vhfantes.fantesday.dto.LoginRequestDTO;
import com.vhfantes.fantesday.dto.MessageDTO;
import com.vhfantes.fantesday.dto.RefreshTokenRequestDTO;
import com.vhfantes.fantesday.dto.ResetPasswordRequestDTO;
import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.service.AuthService;
import com.vhfantes.fantesday.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    // Publico: cria a conta e devolve os tokens
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@RequestBody UserRequestDTO dto) {
        return authService.register(dto);
    }

    // Publico: troca e-mail e senha pelos tokens
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    // Publico: renova o JWT usando o refresh token (tambem usado no login por Face ID)
    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@RequestBody RefreshTokenRequestDTO dto) {
        return authService.refresh(dto.refreshToken());
    }

    // Publico: invalida o refresh token deste aparelho
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshTokenRequestDTO dto) {
        authService.logout(dto.refreshToken());
    }

    // Publico: gera o codigo de recuperacao (a resposta e a mesma exista o e-mail ou nao)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageDTO forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        passwordResetService.requestCode(dto.email());
        return new MessageDTO("Se o e-mail estiver cadastrado, um codigo de recuperacao foi enviado");
    }

    // Publico: define a senha nova com o codigo recebido
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody ResetPasswordRequestDTO dto) {
        passwordResetService.resetPassword(dto.email(), dto.code(), dto.newPassword());
    }

    // Logado: troca a senha (exige a senha atual) e devolve tokens novos
    @PostMapping("/change-password")
    public AuthResponseDTO changePassword(@RequestBody ChangePasswordRequestDTO dto) {
        return authService.changePassword(dto);
    }

    // Usuario dono do token
    @GetMapping("/me")
    public UserResponseDTO me() {
        return authService.me();
    }
}
