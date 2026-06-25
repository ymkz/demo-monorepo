package dev.ymkz.demo.api.external.jsonplaceholder;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JsonplaceholderApi {

    private final JsonplaceholderApiConfig config;

    private final RestClient restClient;

    public JsonplaceholderApi(RestClient.Builder restClientBuilder, JsonplaceholderApiConfig config) {
        this.config = config;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.connectTimeout());
        factory.setReadTimeout(config.readTimeout());
        this.restClient = restClientBuilder.requestFactory(factory).build();
    }

    public List<UsersApiResponse> getUsers() {
        return restClient
                .get()
                .uri(config.url())
                .retrieve()
                .body(new ParameterizedTypeReference<List<UsersApiResponse>>() {});
    }
}
