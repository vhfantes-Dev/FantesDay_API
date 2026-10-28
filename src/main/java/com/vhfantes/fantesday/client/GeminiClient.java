package com.vhfantes.fantesday.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.vhfantes.fantesday.client.dto.GeminiResponse;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// Client da API do Gemini (https://ai.google.dev/api/generate-content).
// Pede a resposta em JSON no formato de um schema e converte para o tipo informado.
// Erros de rede, do servidor ou uma resposta fora do formato sobem como RestClientException.
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String model;
    private final boolean configured;

    // Espera antes de tentar de novo quando o Gemini esta sobrecarregado (os testes usam zero)
    Duration retryDelay = Duration.ofSeconds(1);

    public GeminiClient(
            @Qualifier("geminiRestClient") RestClient restClient,
            JsonMapper jsonMapper,
            @Value("${app.gemini.model}") String model,
            @Value("${app.gemini.api-key}") String apiKey) {
        this.restClient = restClient;
        this.jsonMapper = jsonMapper;
        this.model = model;
        this.configured = apiKey != null && !apiKey.isBlank();
    }

    // Sem GEMINI_API_KEY as funcionalidades de IA ficam desligadas
    public boolean isConfigured() {
        return configured;
    }

    public <T> T generateJson(String prompt, Image image, Map<String, Object> schema, Class<T> type)
            throws RestClientException {
        List<Map<String, Object>> parts = new ArrayList<>();
        if (image != null) {
            parts.add(Map.of("inlineData", Map.of(
                    "mimeType", image.mimeType(),
                    "data", Base64.getEncoder().encodeToString(image.data()))));
        }
        parts.add(Map.of("text", prompt));

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("role", "user", "parts", parts)),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", schema));

        String json = textOf(send(body));
        try {
            return jsonMapper.readValue(json, type);
        } catch (JacksonException e) {
            throw new RestClientException("Resposta do Gemini fora do formato esperado", e);
        }
    }

    // Modelo sobrecarregado (503) ou limite de requisicoes (429) costumam passar rapido: tenta mais uma vez
    private GeminiResponse send(Map<String, Object> body) {
        try {
            return post(body);
        } catch (HttpServerErrorException.ServiceUnavailable | HttpClientErrorException.TooManyRequests e) {
            try {
                Thread.sleep(retryDelay.toMillis());
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw e;
            }
            return post(body);
        }
    }

    private GeminiResponse post(Map<String, Object> body) {
        return restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .body(body)
                .retrieve()
                .body(GeminiResponse.class);
    }

    private static String textOf(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new RestClientException("Gemini nao devolveu resposta");
        }
        GeminiResponse.Candidate candidate = response.candidates().get(0);
        if (candidate.content() == null || candidate.content().parts() == null) {
            throw new RestClientException("Gemini nao devolveu resposta (motivo: " + candidate.finishReason() + ")");
        }

        StringBuilder text = new StringBuilder();
        candidate.content().parts().stream()
                .filter(part -> !Boolean.TRUE.equals(part.thought()))
                .map(GeminiResponse.Part::text)
                .filter(Objects::nonNull)
                .forEach(text::append);
        if (text.isEmpty()) {
            throw new RestClientException("Gemini devolveu uma resposta vazia");
        }
        return text.toString();
    }

    // Imagem enviada junto com o prompt (ex: foto de uma refeicao)
    public record Image(byte[] data, String mimeType) {
    }
}
