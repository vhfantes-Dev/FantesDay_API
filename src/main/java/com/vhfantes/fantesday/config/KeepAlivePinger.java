package com.vhfantes.fantesday.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Mantem a API acordada no plano gratuito do Render, que desliga o servico depois de ~15 min sem requisicoes.
// O job chama a URL publica da propria API (passando pelo proxy do Render, o que conta como trafego de entrada).
// A URL vem de KEEP_ALIVE_URL ou de RENDER_EXTERNAL_URL (definida automaticamente pelo Render);
// sem nenhuma das duas (ex: rodando local), o job nao faz nada.
@Component
@EnableScheduling
public class KeepAlivePinger {

    private static final Logger logger = LoggerFactory.getLogger(KeepAlivePinger.class);
    private static final String HEALTH_PATH = "/actuator/health";

    private final String baseUrl;
    private final RestClient restClient;
    private int consecutiveFailures;

    public KeepAlivePinger(@Value("${app.keep-alive.url:}") String baseUrl, RestClient.Builder builder) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(20));
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStatus() {
        if (baseUrl.isEmpty()) {
            logger.info("Keep-alive desligado (defina KEEP_ALIVE_URL ou rode no Render)");
        } else {
            logger.info("Keep-alive ligado: {}{}", baseUrl, HEALTH_PATH);
        }
    }

    @Scheduled(initialDelayString = "${app.keep-alive.interval}", fixedDelayString = "${app.keep-alive.interval}")
    public void ping() {
        if (baseUrl.isEmpty()) {
            return;
        }
        try {
            restClient.get().uri(baseUrl + HEALTH_PATH).retrieve().toBodilessEntity();
            if (consecutiveFailures > 0) {
                logger.info("Keep-alive voltou a responder depois de {} falhas", consecutiveFailures);
            }
            consecutiveFailures = 0;
        } catch (RestClientException e) {
            consecutiveFailures++;
            // Avisa na primeira falha e depois a cada 10, para nao encher o log
            if (consecutiveFailures == 1 || consecutiveFailures % 10 == 0) {
                logger.warn("Keep-alive falhou ({} seguidas): {}", consecutiveFailures, e.getMessage());
            }
        }
    }
}
