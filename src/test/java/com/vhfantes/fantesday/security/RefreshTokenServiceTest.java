package com.vhfantes.fantesday.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vhfantes.fantesday.entity.RefreshToken;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.UnauthorizedException;
import com.vhfantes.fantesday.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, Duration.ofDays(30));
        user = new User();
        user.setId(5L);
    }

    @Test
    void issuedTokenIsStoredOnlyAsHash() {
        RefreshTokenService.IssuedRefreshToken issued = service.issue(user);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo(SecureTokens.sha256Hex(issued.value()));
        assertThat(saved.getValue().getTokenHash()).isNotEqualTo(issued.value());
        assertThat(issued.expiresAt()).isAfter(Instant.now().plus(Duration.ofDays(29)));
    }

    @Test
    void consumingRevokesTheTokenAndReturnsTheUser() {
        RefreshToken token = token(Instant.now().plus(Duration.ofDays(1)), null);
        when(repository.findByTokenHash(SecureTokens.sha256Hex("abc"))).thenReturn(Optional.of(token));

        assertThat(service.consume("abc")).isSameAs(user);
        assertThat(token.getRevokedAt()).isNotNull();
    }

    @Test
    void reusingARevokedTokenEndsAllSessions() {
        RefreshToken token = token(Instant.now().plus(Duration.ofDays(1)), Instant.now().minusSeconds(60));
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("abc")).isInstanceOf(UnauthorizedException.class);
        verify(repository).revokeAllByUserId(eq(5L), any(Instant.class));
    }

    @Test
    void expiredTokenIsRejected() {
        RefreshToken token = token(Instant.now().minusSeconds(1), null);
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("abc")).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void unknownTokenIsRejected() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consume("abc")).isInstanceOf(UnauthorizedException.class);
    }

    private RefreshToken token(Instant expiresAt, Instant revokedAt) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(revokedAt);
        return token;
    }
}
