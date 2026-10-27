package com.vhfantes.fantesday.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.vhfantes.fantesday.client.dto.OffProduct;

class OpenFoodFactsClientTest {

    private static final String BASE_URL = "https://off.test";

    private MockRestServiceServer server;
    private OpenFoodFactsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenFoodFactsClient(builder.build());
    }

    @Test
    void findByBarcodeReturnsProduct() {
        server.expect(requestTo(startsWith(BASE_URL + "/api/v2/product/7891000100103")))
                .andRespond(withSuccess("""
                        {"code":"7891000100103","status":1,"product":{
                          "code":"7891000100103","product_name":"Nescau","product_name_pt":"Nescau 2.0",
                          "brands":"Nestle","nutriments":{"energy-kcal_100g":380,"proteins_100g":"4.5",
                          "carbohydrates_100g":85.2,"fat_100g":2.1,"fiber_100g":5}}}
                        """, MediaType.APPLICATION_JSON));

        Optional<OffProduct> result = client.findByBarcode("7891000100103");

        assertThat(result).isPresent();
        assertThat(result.get().displayName()).isEqualTo("Nescau 2.0");
        assertThat(result.get().nutriments().energyKcal100g()).isEqualByComparingTo("380");
        assertThat(result.get().nutriments().proteins100g()).isEqualByComparingTo(new BigDecimal("4.5"));
        server.verify();
    }

    @Test
    void findByBarcodeReturnsEmptyWhenStatusIsZero() {
        server.expect(requestTo(startsWith(BASE_URL + "/api/v2/product/000")))
                .andRespond(withSuccess("{\"code\":\"000\",\"status\":0}", MediaType.APPLICATION_JSON));

        assertThat(client.findByBarcode("000")).isEmpty();
    }

    @Test
    void findByBarcodeReturnsEmptyOn404() {
        server.expect(requestTo(startsWith(BASE_URL + "/api/v2/product/111")))
                .andRespond(withResourceNotFound());

        assertThat(client.findByBarcode("111")).isEmpty();
    }

    @Test
    void findByBarcodePropagatesServerError() {
        server.expect(requestTo(startsWith(BASE_URL + "/api/v2/product/222")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findByBarcode("222")).isInstanceOf(RestClientException.class);
    }

    @Test
    void searchReturnsProducts() {
        server.expect(requestTo(containsString("search_terms=biscoito")))
                .andRespond(withSuccess("""
                        {"count":1,"products":[{"code":"789","product_name":"Biscoito"}]}
                        """, MediaType.APPLICATION_JSON));

        List<OffProduct> products = client.search("biscoito");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).displayName()).isEqualTo("Biscoito");
    }
}
