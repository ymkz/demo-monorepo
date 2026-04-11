package dev.ymkz.demo.api.features.books;

import static io.restassured.RestAssured.given;

import dev.ymkz.demo.api.support.testing.TestContainersConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfig.class)
public class SearchBooksIntTest {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void searchBooksが200でレスポンスされること() {
        given().contentType(ContentType.JSON).when().get("/books").then().statusCode(200);
    }

    @Test
    void バリデーションエラー時に400を返す() {
        // limit=101は@Max(100)違反
        given().contentType(ContentType.JSON)
                .when()
                .get("/books?limit=101")
                .then()
                .statusCode(400);
    }

    @Test
    void 存在しないパスにアクセスすると404を返す() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/nonexistent-path")
                .then()
                .statusCode(404);
    }
}
