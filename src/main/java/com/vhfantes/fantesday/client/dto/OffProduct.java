package com.vhfantes.fantesday.client.dto;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProduct(
        String code,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_name_pt") String productNamePt,
        String brands,
        String quantity,
        @JsonProperty("product_quantity_unit") String productQuantityUnit,
        OffNutriments nutriments) {

    // "350 ml", "2L", "1,5 litros"...
    private static final Pattern LIQUID_QUANTITY = Pattern.compile("(?i)\\d\\s*(ml|cl|l|litros?)\\b");

    // Bebidas: o Open Food Facts informa os nutrientes por 100 ml, entao a densidade usada e 1
    public boolean isLiquid() {
        if (productQuantityUnit != null && productQuantityUnit.trim().equalsIgnoreCase("ml")) {
            return true;
        }
        return quantity != null && LIQUID_QUANTITY.matcher(quantity).find();
    }

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
