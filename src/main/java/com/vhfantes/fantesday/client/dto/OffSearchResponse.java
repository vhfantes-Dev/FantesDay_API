package com.vhfantes.fantesday.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Resposta de GET /cgi/search.pl
@JsonIgnoreProperties(ignoreUnknown = true)
public record OffSearchResponse(
        Integer count,
        List<OffProduct> products) {
}
