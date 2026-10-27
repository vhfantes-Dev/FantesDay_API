package com.vhfantes.fantesday.exception;

// Usuario autenticado tentando acessar dados de outro usuario (vira HTTP 403)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
