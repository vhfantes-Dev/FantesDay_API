package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.vhfantes.fantesday.dto.DashboardDTO;
import com.vhfantes.fantesday.dto.ProgressResponseDTO;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.security.PasswordPolicy;

// Regras pequenas e puras de varios services
class SmallRulesTest {

    @Test
    void activityXpIsOnePerTwoMinutesWithCap() {
        ActivityService service = new ActivityService(null, null, null, null);
        ReflectionTestUtils.setField(service, "minutesPerXp", 2);
        ReflectionTestUtils.setField(service, "maxXpPerActivity", 60);

        assertThat(service.xpFor(30)).isEqualTo(15);
        assertThat(service.xpFor(1)).isZero();
        assertThat(service.xpFor(500)).isEqualTo(60);
        assertThat(service.xpFor(null)).isZero();
    }

    @Test
    void detectsImageTypeByContent() {
        assertThat(UserPhotoService.detectImageType(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00 }))
                .isEqualTo("image/jpeg");
        assertThat(UserPhotoService.detectImageType(new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A }))
                .isEqualTo("image/png");
        assertThat(UserPhotoService.detectImageType("RIFF1234WEBP".getBytes())).isEqualTo("image/webp");
        assertThat(UserPhotoService.detectImageType("<svg></svg>".getBytes())).isNull();
    }

    @Test
    void progressSummaryShowsXpToNextLevel() {
        DashboardDTO.ProgressSummary summary = DashboardService.progressSummary(
                new ProgressResponseDTO(1L, 250, 3, null, LocalDateTime.now()));

        assertThat(summary.xpToNextLevel()).isEqualTo(50);
        assertThat(summary.levelProgressPercent()).isEqualTo(50);
    }

    @Test
    void passwordPolicyRequiresEightCharacters() {
        assertThatThrownBy(() -> PasswordPolicy.validate("1234567")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> PasswordPolicy.validate(null)).isInstanceOf(BusinessException.class);
        PasswordPolicy.validate("12345678");
    }
}
