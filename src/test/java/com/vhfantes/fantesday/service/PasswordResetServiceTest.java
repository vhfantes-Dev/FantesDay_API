package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.vhfantes.fantesday.entity.PasswordResetCode;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.repository.PasswordResetCodeRepository;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.security.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final String EMAIL = "teste@fantesday.test";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetCodeRepository codeRepository;
    @Mock
    private RefreshTokenService refreshTokenService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(userRepository, codeRepository, encoder, refreshTokenService,
                Duration.ofMinutes(15), 3);
        user = new User();
        user.setId(9L);
        user.setEmail(EMAIL);
        user.setPasswordHash(encoder.encode("senhaAntiga1"));
    }

    @Test
    void unknownEmailDoesNotCreateCode() {
        when(userRepository.findByEmail("ninguem@fantesday.test")).thenReturn(Optional.empty());

        service.requestCode("ninguem@fantesday.test");

        verify(codeRepository, never()).save(any());
    }

    @Test
    void correctCodeChangesPasswordAndEndsSessions() {
        PasswordResetCode code = code("123456", Instant.now().plusSeconds(600));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(codeRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(9L)).thenReturn(Optional.of(code));

        service.resetPassword(EMAIL, "123456", "senhaNova123");

        assertThat(encoder.matches("senhaNova123", user.getPasswordHash())).isTrue();
        assertThat(code.getUsedAt()).isNotNull();
        verify(refreshTokenService).revokeAll(9L);
    }

    @Test
    void wrongCodeCountsAttemptsAndBlocksAfterTheLimit() {
        PasswordResetCode code = code("123456", Instant.now().plusSeconds(600));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(codeRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(9L)).thenReturn(Optional.of(code));

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> service.resetPassword(EMAIL, "000000", "senhaNova123"))
                    .isInstanceOf(BusinessException.class);
        }

        assertThat(code.getAttempts()).isEqualTo(3);
        assertThat(code.getUsedAt()).isNotNull(); // codigo invalidado
        assertThat(encoder.matches("senhaAntiga1", user.getPasswordHash())).isTrue();
    }

    @Test
    void expiredCodeIsRejected() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(codeRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(9L))
                .thenReturn(Optional.of(code("123456", Instant.now().minusSeconds(1))));

        assertThatThrownBy(() -> service.resetPassword(EMAIL, "123456", "senhaNova123"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void masksEmailInLogs() {
        assertThat(PasswordResetService.maskEmail("vitor@exemplo.com")).isEqualTo("v***@exemplo.com");
    }

    private PasswordResetCode code(String value, Instant expiresAt) {
        PasswordResetCode code = new PasswordResetCode();
        code.setUser(user);
        code.setCodeHash(encoder.encode(value));
        code.setExpiresAt(expiresAt);
        return code;
    }
}
