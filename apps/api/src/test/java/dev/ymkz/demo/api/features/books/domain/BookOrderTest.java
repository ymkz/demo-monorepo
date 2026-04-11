package dev.ymkz.demo.api.features.books.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BookOrderTest {

    @Test
    void 有効な並び順文字列を変換すると対応する列挙値が返ること() {
        var order = BookOrder.fromString("+price");

        assertThat(order).isEqualTo(BookOrder.PRICE_ASC);
    }

    @Test
    void 不正な並び順文字列を変換すると例外が送出されること() {
        assertThatThrownBy(() -> BookOrder.fromString("+invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid BookOrder value: +invalid");
    }

    @Test
    void 価格昇順のSQL表現を取得できること() {
        assertThat(BookOrder.PRICE_ASC.getOrderBy()).isEqualTo("price ASC");
    }

    @Test
    void 価格降順のSQL表現を取得できること() {
        assertThat(BookOrder.PRICE_DESC.getOrderBy()).isEqualTo("price DESC");
    }
}
