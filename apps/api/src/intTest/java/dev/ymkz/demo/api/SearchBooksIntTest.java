package dev.ymkz.demo.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchBooksIntTest {

    @LocalServerPort
    private Integer port;

    @TestConfiguration(proxyBeanMethods = false)
    @Import(DemoApiApplication.class)
    static class TestConfig {
        @Bean
        @ServiceConnection
        MySQLContainer<?> mysql() {
            return new MySQLContainer<>("mysql:8");
        }
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void searchBooksが200でレスポンスされること() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/books")
            .then()
            .statusCode(200)
            .body(".", hasSize(2));
    }
}
