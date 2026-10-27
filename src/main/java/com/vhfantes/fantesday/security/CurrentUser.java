package com.vhfantes.fantesday.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.vhfantes.fantesday.exception.ForbiddenException;
import com.vhfantes.fantesday.exception.UnauthorizedException;

// Usuario dono do token da requisicao atual
@Component
public class CurrentUser {

    public Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            return Long.valueOf(jwt.getToken().getSubject());
        }
        throw new UnauthorizedException("Usuario nao autenticado");
    }

    // Garante que o usuario so acesse os proprios dados
    public void checkAccess(Long ownerId) {
        if (ownerId == null || !ownerId.equals(id())) {
            throw new ForbiddenException("Acesso negado");
        }
    }
}
