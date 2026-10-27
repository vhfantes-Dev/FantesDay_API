package com.vhfantes.fantesday.service;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.entity.PasswordResetCode;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.repository.PasswordResetCodeRepository;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.security.PasswordPolicy;
import com.vhfantes.fantesday.security.RefreshTokenService;
import com.vhfantes.fantesday.security.SecureTokens;

// "Esqueci a senha": gera um codigo de 6 digitos e troca a senha com ele.
// Por enquanto o codigo NAO e enviado por e-mail: ele aparece no log da API (veja o README).
@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String INVALID_CODE = "Codigo invalido ou expirado";

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final Duration codeExpiration;
    private final int maxAttempts;

    public PasswordResetService(UserRepository userRepository, PasswordResetCodeRepository codeRepository,
            PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService,
            @Value("${app.password-reset.code-expiration}") Duration codeExpiration,
            @Value("${app.password-reset.max-attempts}") int maxAttempts) {
        this.userRepository = userRepository;
        this.codeRepository = codeRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.codeExpiration = codeExpiration;
        this.maxAttempts = maxAttempts;
    }

    // Sempre "da certo" para quem chama, exista o e-mail ou nao, para nao revelar quais e-mails tem conta
    @Transactional
    public void requestCode(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("E-mail e obrigatorio");
        }

        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            codeRepository.invalidateAllByUserId(user.getId(), Instant.now());

            String code = SecureTokens.randomSixDigitCode();
            PasswordResetCode resetCode = new PasswordResetCode();
            resetCode.setUser(user);
            resetCode.setCodeHash(passwordEncoder.encode(code));
            resetCode.setExpiresAt(Instant.now().plus(codeExpiration));
            codeRepository.save(resetCode);

            // TODO: enviar por e-mail quando houver um servico de e-mail configurado
            logger.info("Codigo de recuperacao de senha para {}: {} (valido ate {})",
                    maskEmail(user.getEmail()), code, resetCode.getExpiresAt());
        });
    }

    // noRollbackFor: as tentativas erradas precisam ser gravadas mesmo quando a resposta e erro
    @Transactional(noRollbackFor = BusinessException.class)
    public void resetPassword(String email, String code, String newPassword) {
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new BusinessException("E-mail e codigo sao obrigatorios");
        }
        PasswordPolicy.validate(newPassword);

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(INVALID_CODE));
        PasswordResetCode resetCode = codeRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())
                .filter(found -> found.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new BusinessException(INVALID_CODE));

        int attempts = (resetCode.getAttempts() != null ? resetCode.getAttempts() : 0) + 1;
        resetCode.setAttempts(attempts);

        if (!passwordEncoder.matches(code.trim(), resetCode.getCodeHash())) {
            if (attempts >= maxAttempts) {
                resetCode.setUsedAt(Instant.now());
                throw new BusinessException("Muitas tentativas. Peca um novo codigo");
            }
            throw new BusinessException(INVALID_CODE);
        }

        resetCode.setUsedAt(Instant.now());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        // Senha nova: encerra as sessoes abertas em outros aparelhos
        refreshTokenService.revokeAll(user.getId());
    }

    // "vitor@exemplo.com" -> "v***@exemplo.com"
    static String maskEmail(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" + email.substring(Math.max(at, 0)) : email.charAt(0) + "***" + email.substring(at);
    }
}
