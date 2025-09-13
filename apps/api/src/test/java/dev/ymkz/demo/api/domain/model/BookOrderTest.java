package dev.ymkz.demo.api.domain.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BookOrderTest {

    @Test
    void testFromStringValid() {
        // given
        String value = "+price";

        // when
        BookOrder order = BookOrder.fromString(value);

        // then
        assertThat(order, is(BookOrder.PRICE_ASC));
    }

    @Test
    void testFromStringInvalid() {
        // given
        String value = "+invalid";

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            BookOrder.fromString(value);
        });

        // then
        assertThat(exception.getMessage(), is("Invalid BookOrder value: +invalid"));
    }

    @Test
    void testGetOrderByAsc() {
        // given
        BookOrder order = BookOrder.PRICE_ASC;

        // when
        String orderBy = order.getOrderBy();

        // then
        assertThat(orderBy, is("price ASC"));
    }

    @Test
    void testGetOrderByDesc() {
        // given
        BookOrder order = BookOrder.PRICE_DESC;

        // when
        String orderBy = order.getOrderBy();

        // then
        assertThat(orderBy, is("price DESC"));
    }
}
