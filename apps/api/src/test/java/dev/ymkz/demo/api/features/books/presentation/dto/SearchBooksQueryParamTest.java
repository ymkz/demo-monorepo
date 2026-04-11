package dev.ymkz.demo.api.features.books.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ymkz.demo.api.features.books.domain.BookOrder;
import dev.ymkz.demo.api.features.books.domain.BookStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchBooksQueryParamTest {

    @Test
    void orderが未指定でも出版日降順でBookSearchQueryへ変換されること() {
        var queryParam = new SearchBooksQueryParam(
                "9783832185923",
                "ノルウェイの森",
                1000,
                2000,
                List.of(BookStatus.PUBLISHED),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z"),
                null);

        var query = queryParam.toBookSearchQuery(10, 20);

        assertThat(query.isbn()).isNotNull();
        assertThat(query.isbn().value()).isEqualTo("9783832185923");
        assertThat(query.title()).isEqualTo("ノルウェイの森");
        assertThat(query.priceRange().min()).isEqualTo(1000);
        assertThat(query.priceRange().max()).isEqualTo(2000);
        assertThat(query.statuses()).containsExactly(BookStatus.PUBLISHED);
        assertThat(query.publishedAtRange().start()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
        assertThat(query.publishedAtRange().end()).isEqualTo(Instant.parse("2025-12-31T23:59:59Z"));
        assertThat(query.order()).isEqualTo(BookOrder.PUBLISHED_AT_DESC);
        assertThat(query.offset()).isEqualTo(10);
        assertThat(query.limit()).isEqualTo(20);
    }

    @Test
    void ダウンロード用に変換するとページング条件をnullで渡せること() {
        var queryParam = new SearchBooksQueryParam(null, null, null, null, null, null, null, BookOrder.PRICE_ASC);

        var query = queryParam.toBookSearchQuery(null, null);

        assertThat(query.isbn()).isNull();
        assertThat(query.priceRange().min()).isNull();
        assertThat(query.priceRange().max()).isNull();
        assertThat(query.publishedAtRange().start()).isNull();
        assertThat(query.publishedAtRange().end()).isNull();
        assertThat(query.order()).isEqualTo(BookOrder.PRICE_ASC);
        assertThat(query.offset()).isNull();
        assertThat(query.limit()).isNull();
    }
}
