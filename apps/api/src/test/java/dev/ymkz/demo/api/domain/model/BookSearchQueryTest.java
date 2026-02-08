package dev.ymkz.demo.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ymkz.demo.core.domain.valueobject.Isbn;
import dev.ymkz.demo.core.domain.valueobject.RangeInteger;
import dev.ymkz.demo.core.domain.valueobject.RangeTime;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookSearchQueryTest {

    @Test
    void 全パラメータ指定で構築できること() {
        var isbn = Isbn.of("9783832185923");
        var title = "ノルウェイの森";
        var priceRange = RangeInteger.of(1000, 2000);
        var statuses = List.of(BookStatus.PUBLISHED);
        var publishedAtRange = RangeTime.of(
                LocalDateTime.of(1980, 1, 1, 0, 0)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant(),
                LocalDateTime.of(1990, 1, 1, 0, 0)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant());
        var order = BookOrder.PRICE_ASC;
        var offset = 0;
        var limit = 20;

        var query = new BookSearchQuery(isbn, title, priceRange, statuses, publishedAtRange, order, offset, limit);

        assertThat(query.isbn()).isEqualTo(isbn);
        assertThat(query.title()).isEqualTo(title);
        assertThat(query.priceRange()).isEqualTo(priceRange);
        assertThat(query.statuses()).isEqualTo(statuses);
        assertThat(query.publishedAtRange()).isEqualTo(publishedAtRange);
        assertThat(query.order()).isEqualTo(order);
        assertThat(query.offset()).isEqualTo(offset);
        assertThat(query.limit()).isEqualTo(limit);
    }

    @Test
    void 最小パラメータで構築できること() {
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);

        assertThat(query.isbn()).isNull();
        assertThat(query.title()).isNull();
        assertThat(query.priceRange()).isNull();
        assertThat(query.statuses()).isNull();
        assertThat(query.publishedAtRange()).isNull();
        assertThat(query.order()).isNull();
        assertThat(query.offset()).isNull();
        assertThat(query.limit()).isNull();
    }
}
