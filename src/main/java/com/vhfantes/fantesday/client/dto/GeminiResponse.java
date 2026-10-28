package com.vhfantes.fantesday.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Resposta do generateContent do Gemini (so os campos usados)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(List<Candidate> candidates) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content, String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {
    }

    // thought = true nas partes de "raciocinio" do modelo, que nao fazem parte da resposta
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text, Boolean thought) {
    }
}
