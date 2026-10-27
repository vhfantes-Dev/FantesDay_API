package com.vhfantes.fantesday.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.vhfantes.fantesday.config.SecurityConfig;
import com.vhfantes.fantesday.entity.User;

class TokenServiceTest {

    private static final String SECRET = "segredo-de-teste-com-mais-de-32-caracteres";

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void generatedTokenIsAcceptedByTheDecoder() {
        SecretKey key = config.jwtSecretKey(SECRET);
        TokenService tokenService = new TokenService(config.jwtEncoder(key), Duration.ofHours(1));

        TokenService.IssuedToken token = tokenService.generate(user());
        Jwt jwt = config.jwtDecoder(key).decode(token.value());

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("teste@fantesday.test");
        assertThat(jwt.getExpiresAt()).isEqualTo(token.expiresAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        TokenService otherIssuer = new TokenService(
                config.jwtEncoder(config.jwtSecretKey("outro-segredo-tambem-com-mais-de-32-caracteres")), Duration.ofHours(1));
        JwtDecoder decoder = config.jwtDecoder(config.jwtSecretKey(SECRET));

        String token = otherIssuer.generate(user()).value();

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        SecretKey key = config.jwtSecretKey(SECRET);
        Instant twoHoursAgo = Instant.now().minus(Duration.ofHours(2));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(TokenService.ISSUER)
                .subject("42")
                .issuedAt(twoHoursAgo)
                .expiresAt(twoHoursAgo.plus(Duration.ofHours(1)))
                .build();
        String token = config.jwtEncoder(key)
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThatThrownBy(() -> config.jwtDecoder(key).decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void shortSecretIsRejected() {
        assertThatThrownBy(() -> config.jwtSecretKey("curto")).isInstanceOf(IllegalStateException.class);
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setName("Usuario Teste");
        user.setEmail("teste@fantesday.test");
        return user;
    }
}
