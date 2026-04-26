package dev.ymkz.demo.api.features.books;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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
    void 価格降順を指定すると価格の降順でレスポンスされること() {
        given().contentType(ContentType.JSON)
                .queryParam("order", "-price")
                .queryParam("priceFrom", 0)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("items.price", contains(12000, 5500, 1000, 780));
    }

    @Test
    void 書籍の作成取得更新削除ができること() {
        var isbn = "9784873115658";

        given().contentType(ContentType.JSON)
                .body("""
                        {
                          "isbn": "%s",
                          "title": "リーダブルコード",
                          "price": 2640,
                          "status": "PUBLISHED",
                          "publishedAt": "2012-06-23T00:00:00",
                          "authorId": 1,
                          "publisherId": 1
                        }
                        """.formatted(isbn))
                .when()
                .post("/books")
                .then()
                .statusCode(201);

        var id = given().contentType(ContentType.JSON)
                .queryParam("isbn", isbn)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("items", hasSize(1))
                .body("items[0].title", equalTo("リーダブルコード"))
                .extract()
                .path("items[0].id");

        given().contentType(ContentType.JSON)
                .when()
                .get("/books/{id}", id)
                .then()
                .statusCode(200)
                .body("isbn", equalTo(isbn))
                .body("title", equalTo("リーダブルコード"));

        given().contentType(ContentType.JSON)
                .body("""
                        {
                          "title": "リーダブルコード 改訂版",
                          "price": 3000
                        }
                        """)
                .when()
                .patch("/books/{id}", id)
                .then()
                .statusCode(200);

        given().contentType(ContentType.JSON)
                .when()
                .get("/books/{id}", id)
                .then()
                .statusCode(200)
                .body("title", equalTo("リーダブルコード 改訂版"))
                .body("price", equalTo(3000));

        given().contentType(ContentType.JSON)
                .when()
                .delete("/books/{id}", id)
                .then()
                .statusCode(204);

        given().contentType(ContentType.JSON)
                .when()
                .get("/books/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void 存在しない著者IDで書籍を作成すると400を返すこと() {
        given().contentType(ContentType.JSON)
                .body("""
                        {
                          "isbn": "9784873115658",
                          "title": "リーダブルコード",
                          "price": 2640,
                          "status": "PUBLISHED",
                          "publishedAt": "2012-06-23T00:00:00",
                          "authorId": 99999,
                          "publisherId": 1
                        }
                        """)
                .when()
                .post("/books")
                .then()
                .statusCode(400);
    }

    @Test
    void 存在しない出版社IDで書籍を更新すると400を返すこと() {
        given().contentType(ContentType.JSON)
                .body("""
                        {
                          "publisherId": 99999
                        }
                        """)
                .when()
                .patch("/books/{id}", 1)
                .then()
                .statusCode(400);
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
