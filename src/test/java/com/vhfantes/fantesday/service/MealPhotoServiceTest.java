package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;

import com.vhfantes.fantesday.client.GeminiClient;
import com.vhfantes.fantesday.dto.MealPhotoAnalysisDTO;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class MealPhotoServiceTest {

    private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00 };

    @Mock
    private GeminiClient geminiClient;
    @Mock
    private FoodService foodService;

    @InjectMocks
    private MealPhotoService mealPhotoService;

    @Test
    void returnsItemsWithCatalogMatchAndSumsTotals() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), any(GeminiClient.Image.class), any(), eq(MealPhotoService.AiMeal.class)))
                .thenReturn(new MealPhotoService.AiMeal(true, "Prato feito", List.of(
                        new MealPhotoService.AiItem("Arroz branco", "Arroz, tipo 1, cozido", new BigDecimal("150.4"),
                                new BigDecimal("192"), new BigDecimal("3.8"), new BigDecimal("42.1"), new BigDecimal("0.3"), "alta"),
                        new MealPhotoService.AiItem("Bife", "Carne, bovina, contra-filé, grelhado", new BigDecimal("100"),
                                new BigDecimal("278"), new BigDecimal("32.4"), null, new BigDecimal("15.5"), "duvidosa")),
                        null));

        Food rice = new Food();
        rice.setId(5L);
        rice.setName("Arroz, tipo 1, cozido");
        when(foodService.findBestMatch("Arroz, tipo 1, cozido")).thenReturn(Optional.of(rice));
        when(foodService.findBestMatch("Carne, bovina, contra-filé, grelhado")).thenReturn(Optional.empty());

        MealPhotoAnalysisDTO result = mealPhotoService.analyze(photo(JPEG), "almoco");

        assertThat(result.items()).hasSize(2);
        MealPhotoAnalysisDTO.Item first = result.items().get(0);
        assertThat(first.portionGrams()).isEqualTo(150);
        assertThat(first.foodId()).isEqualTo(5L);
        assertThat(first.confidence()).isEqualTo("ALTA");

        MealPhotoAnalysisDTO.Item second = result.items().get(1);
        assertThat(second.foodId()).isNull();
        assertThat(second.carbs()).isEqualByComparingTo("0");
        assertThat(second.confidence()).isEqualTo("BAIXA");

        assertThat(result.totals().calories()).isEqualTo(470);
        assertThat(result.totals().protein()).isEqualByComparingTo("36.2");
        assertThat(result.totals().fat()).isEqualByComparingTo("15.8");
    }

    @Test
    void failsWhenPhotoHasNoFood() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), any(), any(), eq(MealPhotoService.AiMeal.class)))
                .thenReturn(new MealPhotoService.AiMeal(false, "Um gato", List.of(), null));

        assertThatThrownBy(() -> mealPhotoService.analyze(photo(JPEG), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nao encontrei comida");
    }

    @Test
    void turnsAiFailureIntoBusinessError() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), any(), any(), eq(MealPhotoService.AiMeal.class)))
                .thenThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> mealPhotoService.analyze(photo(JPEG), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tente novamente");
    }

    @Test
    void rejectsFilesThatAreNotImages() {
        when(geminiClient.isConfigured()).thenReturn(true);

        assertThatThrownBy(() -> mealPhotoService.analyze(photo("<svg></svg>".getBytes()), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Formato invalido");
        verify(geminiClient, never()).generateJson(anyString(), any(), any(), any());
    }

    @Test
    void failsWhenAiIsNotConfigured() {
        when(geminiClient.isConfigured()).thenReturn(false);

        assertThatThrownBy(() -> mealPhotoService.analyze(photo(JPEG), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("GEMINI_API_KEY");
    }

    @Test
    void detectsHeicPhotos() {
        byte[] heic = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c' };
        byte[] heif = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'i', 'f', '1' };

        assertThat(MealPhotoService.detectImageType(heic)).isEqualTo("image/heic");
        assertThat(MealPhotoService.detectImageType(heif)).isEqualTo("image/heif");
        assertThat(MealPhotoService.detectImageType(JPEG)).isEqualTo("image/jpeg");
    }

    private static MockMultipartFile photo(byte[] data) {
        return new MockMultipartFile("file", "prato.jpg", "image/jpeg", data);
    }
}
