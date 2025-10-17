package dev.ymkz.demo.api.infrastructure.externalapi.jsonplaceholder;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JsonplaceholderApi {

    private final RestClient restClient;

    public JsonplaceholderApi(
            RestClient.Builder restClientBuilder,
            @Value("${app.externalapi.jsonplaceholder.base-url}") String baseUrl,
            @Value("${app.externalapi.jsonplaceholder.connect-timeout}") Duration connectTimeout,
            @Value("${app.externalapi.jsonplaceholder.read-timeout}") Duration readTimeout) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
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
