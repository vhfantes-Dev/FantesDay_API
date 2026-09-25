package com.vhfantes.fantesday.exception;

// Registro nao encontrado (vai virar HTTP 404)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
