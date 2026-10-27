package com.vhfantes.fantesday.security;

import com.vhfantes.fantesday.exception.BusinessException;

// Regras minimas de senha (cadastro, troca e recuperacao)
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72; // limite do BCrypt

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Senha e obrigatoria");
        }
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new BusinessException("Senha deve ter entre " + MIN_LENGTH + " e " + MAX_LENGTH + " caracteres");
        }
    }
}
