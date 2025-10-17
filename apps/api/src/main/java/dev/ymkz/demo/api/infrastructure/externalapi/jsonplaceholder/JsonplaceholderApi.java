package dev.ymkz.demo.api.infrastructure.externalapi.jsonplaceholder;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class JsonplaceholderApi {

    private final RestClient restClient;

    JsonplaceholderApi(
            RestClient.Builder restClientBuilder,
            @Value("${app.externalapi.jsonplaceholder.base-url}") String baseUrl,
            @Value("${app.externalapi.jsonplaceholder.connect-timeout-ms}") Duration connectTimeoutMs,
            @Value("${app.externalapi.jsonplaceholder.read-timeout-ms}") Duration readTimeoutMs) {
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restClient =
                restClientBuilder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    public List<UsersApiResponse> getUsers() {
        return restClient
                .get()
                .uri("/users")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UsersApiResponse>>() {});
    }
}
