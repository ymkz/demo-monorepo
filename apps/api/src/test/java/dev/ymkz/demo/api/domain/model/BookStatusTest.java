package dev.ymkz.demo.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

// TODO: BookStatusにfromString()メソッドが実装されたら、null/不正値時の例外テストを追加
class BookStatusTest {

    @Test
    void enumValues_期待される全ての値が存在すること() {
        var expectedValues = List.of("UNPUBLISHED", "PUBLISHED", "OUT_OF_PRINT");

        var actualValues = Arrays.stream(BookStatus.values()).map(Enum::name).toList();

        assertThat(actualValues).containsExactlyInAnyOrderElementsOf(expectedValues);
    }

    @Test
    void unpublishedValue_UNPUBLISHEDが存在すること() {
        assertThat(BookStatus.UNPUBLISHED).isNotNull();
        assertThat(BookStatus.UNPUBLISHED.name()).isEqualTo("UNPUBLISHED");
    }

    @Test
    void publishedValue_PUBLISHEDが存在すること() {
        assertThat(BookStatus.PUBLISHED).isNotNull();
        assertThat(BookStatus.PUBLISHED.name()).isEqualTo("PUBLISHED");
    }

    @Test
    void outOfPrintValue_OUT_OF_PRINTが存在すること() {
        assertThat(BookStatus.OUT_OF_PRINT).isNotNull();
        assertThat(BookStatus.OUT_OF_PRINT.name()).isEqualTo("OUT_OF_PRINT");
    }
}
