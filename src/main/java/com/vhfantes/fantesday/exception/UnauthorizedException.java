package com.vhfantes.fantesday.exception;

// Credenciais invalidas ou usuario nao autenticado (vira HTTP 401)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
