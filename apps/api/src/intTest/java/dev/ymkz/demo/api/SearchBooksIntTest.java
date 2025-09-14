package dev.ymkz.demo.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchBooksIntTest {

    @TestConfiguration(proxyBeanMethods = false)
    @Import(DemoApiApplication.class)
    static class TestConfig {
        @Bean
        @ServiceConnection
        MySQLContainer<?> mysqlContainer() {
            return new MySQLContainer<>("mysql:8");
        }
    }

    @Test
    void shouldGetAllBookmarks() {
        var actual = true;
        var expected = false;
        assertThat(actual).isEqualTo(expected);
    }
}
