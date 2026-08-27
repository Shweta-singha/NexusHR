package org.Employee.client;

import java.time.Duration;
import java.util.List;

import org.Employee.dto.AttritionPredictionRequest;
import org.Employee.dto.AttritionPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Talks to the standalone ai-service FastAPI app for attrition-risk scoring.
 * See ai-service/FEATURE_CONTRACT.md for the request/response shape this
 * client and its DTOs must stay in sync with.
 *
 * First external HTTP client in this project - establishes the pattern for
 * future ones: inject the autoconfigured RestClient.Builder, customize with
 * this service's own base URL and timeout, build once in the constructor.
 */
@Component
public class AttritionClient {

    private final RestClient restClient;

    public AttritionClient(RestClient.Builder builder,
                            @Value("${app.ai-service.url}") String baseUrl) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(5));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }

    public AttritionPredictionResponse predict(AttritionPredictionRequest request) {
        return restClient.post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .body(AttritionPredictionResponse.class);
    }

    public List<AttritionPredictionResponse> predictBatch(List<AttritionPredictionRequest> requests) {
        return restClient.post()
                .uri("/predict/batch")
                .body(requests)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
