package dev.ymkz.demo.api.infrastructure.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ymkz.demo.api.domain.model.BookStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BookEntityTest {

    @Test
    void toBook_正常系_Bookモデルに変換されること() {
        var entity = new BookEntity(
                1L,
                "9783832185923",
                "ノルウェイの森",
                1000,
                BookStatus.PUBLISHED,
                LocalDateTime.of(1987, 9, 4, 0, 0),
                1,
                "村上春樹",
                1,
                "講談社",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0),
                null);

        var book = BookEntity.toBook(entity);

        assertThat(book.id()).isEqualTo(1L);
        assertThat(book.isbn().value()).isEqualTo("9783832185923");
        assertThat(book.title()).isEqualTo("ノルウェイの森");
        assertThat(book.price()).isEqualTo(1000);
        assertThat(book.status()).isEqualTo(BookStatus.PUBLISHED);
        assertThat(book.publishedAt()).isEqualTo(LocalDateTime.of(1987, 9, 4, 0, 0));
        assertThat(book.authorId()).isEqualTo(1);
        assertThat(book.authorName()).isEqualTo("村上春樹");
        assertThat(book.publisherId()).isEqualTo(1);
        assertThat(book.publisherName()).isEqualTo("講談社");
        assertThat(book.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
        assertThat(book.updatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
        assertThat(book.deletedAt()).isNull();
    }

    @Test
    void toBook_nullフィールド_適切にハンドリングされること() {
        var entity = new BookEntity(
                1L,
                "9783832185923",
                "テスト書籍",
                null,
                BookStatus.UNPUBLISHED,
                null,
                1,
                "テスト著者",
                1,
                "テスト出版社",
                LocalDateTime.now(),
                null,
                null);

        var book = BookEntity.toBook(entity);

        assertThat(book.price()).isNull();
        assertThat(book.publishedAt()).isNull();
        assertThat(book.updatedAt()).isNull();
        assertThat(book.deletedAt()).isNull();
    }
}
