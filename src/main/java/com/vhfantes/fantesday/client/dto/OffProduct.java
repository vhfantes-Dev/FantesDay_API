package com.vhfantes.fantesday.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProduct(
        String code,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_name_pt") String productNamePt,
        String brands,
        OffNutriments nutriments) {

    // Prefere o nome em portugues, se existir
    public String displayName() {
        if (productNamePt != null && !productNamePt.isBlank()) {
            return productNamePt.trim();
        }
        if (productName != null && !productName.isBlank()) {
            return productName.trim();
        }
        return null;
    }
}
