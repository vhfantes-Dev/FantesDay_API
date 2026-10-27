package com.vhfantes.fantesday.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Resposta de GET /api/v2/product/{barcode} (status 1 = encontrado, 0 = nao encontrado)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProductResponse(
        String code,
        Integer status,
        OffProduct product) {
}
