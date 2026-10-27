package com.vhfantes.fantesday.security;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.entity.RefreshToken;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.UnauthorizedException;
import com.vhfantes.fantesday.repository.RefreshTokenRepository;

// Refresh tokens com rotacao: cada uso devolve um token novo e invalida o anterior.
// Se um token ja usado aparecer de novo (possivel roubo), todas as sessoes do usuario sao encerradas.
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration expiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration}") Duration expiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expiration = expiration;
    }

    @Transactional
    public IssuedRefreshToken issue(User user) {
        String value = SecureTokens.randomToken();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(SecureTokens.sha256Hex(value));
        token.setExpiresAt(Instant.now().plus(expiration));
        refreshTokenRepository.save(token);

        return new IssuedRefreshToken(value, token.getExpiresAt());
    }

    // Valida o token, invalida-o e devolve o usuario dono (o chamador emite o par novo).
    // noRollbackFor: a revogacao em caso de reuso precisa ser gravada mesmo com o 401.
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public User consume(String value) {
        if (value == null || value.isBlank()) {
            throw new UnauthorizedException("Refresh token e obrigatorio");
        }

        RefreshToken token = refreshTokenRepository.findByTokenHash(SecureTokens.sha256Hex(value))
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));

        if (token.getRevokedAt() != null) {
            logger.warn("Refresh token reutilizado para o usuario {}: encerrando todas as sessoes", token.getUser().getId());
            refreshTokenRepository.revokeAllByUserId(token.getUser().getId(), Instant.now());
            throw new UnauthorizedException("Sessao encerrada. Faca login novamente");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expirado. Faca login novamente");
        }

        token.setRevokedAt(Instant.now());
        return token.getUser();
    }

    // Logout: invalida o token informado (sem erro se ele nao existir ou ja estiver invalido)
    @Transactional
    public void revoke(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(SecureTokens.sha256Hex(value))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    public record IssuedRefreshToken(String value, Instant expiresAt) {
    }
}
