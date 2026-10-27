package com.vhfantes.fantesday.exception;

// Violacao de regra de negocio, ex.: e-mail ja cadastrado (vai virar HTTP 400/409)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
