package com.vhfantes.fantesday.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.UserService;

import lombok.RequiredArgsConstructor;

// A criacao de conta fica em POST /api/auth/register. Cada usuario so acessa a propria conta.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @GetMapping("/{id}")
    public UserResponseDTO findById(@PathVariable Long id) {
        currentUser.checkAccess(id);
        return userService.findById(id);
    }

    // Atualizacao parcial: so os campos enviados sao alterados
    @PatchMapping("/{id}")
    public UserResponseDTO update(@PathVariable Long id, @RequestBody UserRequestDTO dto) {
        currentUser.checkAccess(id);
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        currentUser.checkAccess(id);
        userService.delete(id);
    }
}
