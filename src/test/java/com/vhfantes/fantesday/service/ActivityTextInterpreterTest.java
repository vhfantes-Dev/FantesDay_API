package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import com.vhfantes.fantesday.client.GeminiClient;
import com.vhfantes.fantesday.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class ActivityTextInterpreterTest {

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private ActivityTextInterpreter interpreter;

    @Test
    void knownActivityWithDurationDoesNotCallAi() {
        ActivityTextInterpreter.Result result = interpreter.interpret("joguei volei por 2 horas");

        assertThat(result.activityType()).isEqualTo("Vôlei");
        assertThat(result.durationMinutes()).isEqualTo(120);
        assertThat(result.met()).isEqualByComparingTo("4.0");
        verify(geminiClient, never()).generateJson(anyString(), any(), any(), any());
    }

    @Test
    void unknownActivityIsCompletedByAiWithClampedMet() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), isNull(), any(), eq(ActivityTextInterpreter.AiActivity.class)))
                .thenReturn(new ActivityTextInterpreter.AiActivity(true, "Polo aquático", 50, new BigDecimal("25")));

        ActivityTextInterpreter.Result result = interpreter.interpret("joguei polo aquatico 50 minutos");

        assertThat(result.activityType()).isEqualTo("Polo aquático");
        assertThat(result.durationMinutes()).isEqualTo(50);
        assertThat(result.met()).isEqualByComparingTo("20.0");
    }

    @Test
    void missingDurationIsCompletedByAiButKeepsLocalActivity() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), isNull(), any(), eq(ActivityTextInterpreter.AiActivity.class)))
                .thenReturn(new ActivityTextInterpreter.AiActivity(true, "Corrida leve", 25, new BigDecimal("7")));

        ActivityTextInterpreter.Result result = interpreter.interpret("corri do trabalho ate em casa");

        assertThat(result.activityType()).isEqualTo("Corrida");
        assertThat(result.met()).isEqualByComparingTo("9.8");
        assertThat(result.durationMinutes()).isEqualTo(25);
    }

    @Test
    void withoutDurationAndWithoutAiAsksForDuration() {
        when(geminiClient.isConfigured()).thenReturn(false);

        assertThatThrownBy(() -> interpreter.interpret("joguei volei"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duracao");
    }

    @Test
    void aiFailureFallsBackToLocalResult() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), isNull(), any(), eq(ActivityTextInterpreter.AiActivity.class)))
                .thenThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> interpreter.interpret("fiz algo diferente por 30 minutos"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nao reconheci");
    }

    @Test
    void textThatIsNotAnActivityIsRejected() {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJson(anyString(), isNull(), any(), eq(ActivityTextInterpreter.AiActivity.class)))
                .thenReturn(new ActivityTextInterpreter.AiActivity(false, null, 30, null));

        assertThatThrownBy(() -> interpreter.interpret("assisti filme por 30 minutos"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> interpreter.interpret(" ")).isInstanceOf(BusinessException.class);
    }
}
