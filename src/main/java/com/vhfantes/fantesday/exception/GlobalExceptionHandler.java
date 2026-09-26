package com.vhfantes.fantesday.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Converte as excecoes dos services em respostas HTTP no formato ProblemDetail (RFC 9457):
// { "type", "title", "status", "detail", "instance" }
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // JSON mal formado ou com tipo errado (ex: texto num campo numerico, data fora do formato yyyy-MM-dd)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido");
    }

    // Parametro da URL com tipo errado (ex: /api/users/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "Valor invalido para o parametro '" + e.getName() + "'");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException e) {
        return problem(HttpStatus.BAD_REQUEST, "Parametro obrigatorio ausente: " + e.getParameterName());
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
