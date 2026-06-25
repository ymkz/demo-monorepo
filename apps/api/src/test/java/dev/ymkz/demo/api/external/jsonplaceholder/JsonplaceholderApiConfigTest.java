package dev.ymkz.demo.api.external.jsonplaceholder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

class JsonplaceholderApiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Test
    void 有効な設定値の場合はJsonplaceholderApiConfigへバインドされること() {
        contextRunner
                .withPropertyValues(
                        "app.externalapi.jsonplaceholder.url=https://jsonplaceholder.typicode.com/users",
                        "app.externalapi.jsonplaceholder.connect-timeout=500ms",
                        "app.externalapi.jsonplaceholder.read-timeout=3000ms")
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    var config = context.getBean(JsonplaceholderApiConfig.class);
                    assertThat(config.url()).isEqualTo("https://jsonplaceholder.typicode.com/users");
                    assertThat(config.connectTimeout()).isEqualTo(Duration.ofMillis(500));
                    assertThat(config.readTimeout()).isEqualTo(Duration.ofSeconds(3));
                });
    }

    @Test
    void urlが空文字の場合は起動時バリデーションで失敗すること() {
        contextRunner
                .withPropertyValues(
                        "app.externalapi.jsonplaceholder.url=",
                        "app.externalapi.jsonplaceholder.connect-timeout=500ms",
                        "app.externalapi.jsonplaceholder.read-timeout=3000ms")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void connectTimeoutが0ミリ秒の場合は起動時バリデーションで失敗すること() {
        contextRunner
                .withPropertyValues(
                        "app.externalapi.jsonplaceholder.url=https://jsonplaceholder.typicode.com/users",
                        "app.externalapi.jsonplaceholder.connect-timeout=0ms",
                        "app.externalapi.jsonplaceholder.read-timeout=3000ms")
                .run(context -> assertThat(context).hasFailed());
    }

    @EnableConfigurationProperties(JsonplaceholderApiConfig.class)
    static class TestConfig {}
}
