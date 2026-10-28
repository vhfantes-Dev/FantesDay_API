package com.vhfantes.fantesday.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // O Open Food Facts pede um User-Agent identificando o app (nome/versao + contato)
    @Bean
    public RestClient openFoodFactsRestClient(
            RestClient.Builder builder,
            @Value("${app.openfoodfacts.base-url}") String baseUrl,
            @Value("${app.openfoodfacts.user-agent}") String userAgent) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .requestFactory(requestFactory)
                .build();
    }

    // A chave vai no header (e nao na URL) para nao aparecer em logs de requisicao
    @Bean
    public RestClient geminiRestClient(
            RestClient.Builder builder,
            @Value("${app.gemini.base-url}") String baseUrl,
            @Value("${app.gemini.api-key}") String apiKey) {

        // Analisar uma foto pode levar alguns segundos
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .requestFactory(requestFactory)
                .build();
    }
}
