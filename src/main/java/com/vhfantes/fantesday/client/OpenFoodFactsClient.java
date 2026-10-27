package com.vhfantes.fantesday.client;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.vhfantes.fantesday.client.dto.OffProduct;
import com.vhfantes.fantesday.client.dto.OffProductResponse;
import com.vhfantes.fantesday.client.dto.OffSearchResponse;

// Client da API publica do Open Food Facts (https://openfoodfacts.github.io/openfoodfacts-server/api/)
// Erros de rede ou do servidor sobem como RestClientException para o service decidir o que fazer.
@Component
public class OpenFoodFactsClient {

    private static final String FIELDS = "code,product_name,product_name_pt,brands,quantity,product_quantity_unit,nutriments";
    private static final int SEARCH_PAGE_SIZE = 20;

    private final RestClient restClient;

    public OpenFoodFactsClient(@Qualifier("openFoodFactsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<OffProduct> findByBarcode(String barcode) throws RestClientException {
        try {
            OffProductResponse response = restClient.get()
                    .uri("/api/v2/product/{barcode}?fields={fields}", barcode, FIELDS)
                    .retrieve()
                    .body(OffProductResponse.class);

            if (response == null || response.status() == null || response.status() != 1) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.product());
        } catch (HttpClientErrorException.NotFound e) {
            // A API v2 responde 404 quando o codigo de barras nao existe
            return Optional.empty();
        }
    }

    // Busca por nome, priorizando produtos vendidos no Brasil
    public List<OffProduct> search(String term) throws RestClientException {
        OffSearchResponse response = restClient.get()
                .uri(uri -> uri.path("/cgi/search.pl")
                        .queryParam("search_terms", term)
                        .queryParam("search_simple", 1)
                        .queryParam("action", "process")
                        .queryParam("json", 1)
                        .queryParam("page_size", SEARCH_PAGE_SIZE)
                        .queryParam("fields", FIELDS)
                        .queryParam("lc", "pt")
                        .queryParam("cc", "br")
                        .build())
                .retrieve()
                .body(OffSearchResponse.class);

        if (response == null || response.products() == null) {
            return List.of();
        }
        return response.products();
    }
}
