package com.vhfantes.fantesday.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.json.JsonMapper;

class GeminiClientTest {

    private static final String BASE_URL = "https://gemini.test";
    private static final Map<String, Object> SCHEMA = Map.of("type", "OBJECT");

    private MockRestServiceServer server;
    private GeminiClient client;

    record Answer(String activityName, Integer durationMinutes) {
    }

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GeminiClient(builder.build(), JsonMapper.builder().build(), "gemini-test", "chave");
        client.retryDelay = Duration.ZERO;
    }

    @Test
    void sendsImageAndPromptAndParsesJsonAnswer() {
        server.expect(requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.contents[0].parts[0].inlineData.mimeType").value("image/jpeg"))
                .andExpect(jsonPath("$.contents[0].parts[0].inlineData.data").value("AQID"))
                .andExpect(jsonPath("$.contents[0].parts[1].text").value("prompt"))
                .andExpect(jsonPath("$.generationConfig.responseMimeType").value("application/json"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[
                          {"text":"pensando...","thought":true},
                          {"text":"{\\"activityName\\":\\"Vôlei\\",\\"durationMinutes\\":120}"}
                        ]},"finishReason":"STOP"}]}
                        """, MediaType.APPLICATION_JSON));

        Answer answer = client.generateJson("prompt", new GeminiClient.Image(new byte[] { 1, 2, 3 }, "image/jpeg"),
                SCHEMA, Answer.class);

        assertThat(answer.activityName()).isEqualTo("Vôlei");
        assertThat(answer.durationMinutes()).isEqualTo(120);
        server.verify();
    }

    @Test
    void failsWhenAnswerIsNotTheExpectedJson() {
        server.expect(requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"nao e json"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generateJson("prompt", null, SCHEMA, Answer.class))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void failsWhenThereIsNoCandidate() {
        server.expect(requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andRespond(withSuccess("{\"candidates\":[{\"finishReason\":\"SAFETY\"}]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generateJson("prompt", null, SCHEMA, Answer.class))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("SAFETY");
    }

    @Test
    void retriesOnceWhenModelIsOverloaded() {
        server.expect(requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"activityName\\":\\"Remo\\"}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.generateJson("prompt", null, SCHEMA, Answer.class).activityName()).isEqualTo("Remo");
        server.verify();
    }

    @Test
    void givesUpAfterSecondOverload() {
        server.expect(times(2), requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.generateJson("prompt", null, SCHEMA, Answer.class))
                .isInstanceOf(RestClientException.class);
        server.verify();
    }

    @Test
    void propagatesServerError() {
        server.expect(requestTo(BASE_URL + "/v1beta/models/gemini-test:generateContent"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.generateJson("prompt", null, SCHEMA, Answer.class))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void isNotConfiguredWithoutApiKey() {
        GeminiClient withoutKey = new GeminiClient(RestClient.create(), JsonMapper.builder().build(), "gemini-test", " ");
        assertThat(withoutKey.isConfigured()).isFalse();
        assertThat(client.isConfigured()).isTrue();
    }
}
