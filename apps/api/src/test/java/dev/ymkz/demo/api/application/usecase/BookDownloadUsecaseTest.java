package dev.ymkz.demo.api.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.model.BookStatus;
import dev.ymkz.demo.api.domain.repository.BookRepository;
import dev.ymkz.demo.core.domain.valueobject.Isbn;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TODO: CSV変換失敗時のRuntimeExceptionテストを追加（実装のテスト容易性改善が必要）
@ExtendWith(MockitoExtension.class)
class BookDownloadUsecaseTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookDownloadUsecase bookDownloadUsecase;

    @Test
    void execute_正常系_CSV形式のバイト配列が返ること() {
        // given
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);

        var books = List.of(new Book(
                1L,
                new Isbn("9783832185923"),
                "ノルウェイの森",
                1000,
                BookStatus.PUBLISHED,
                LocalDateTime.of(1987, 9, 4, 0, 0),
                1,
                "村上春樹",
                1,
                "講談社",
                LocalDateTime.now(),
                null,
                null));

        when(repository.download(any(BookSearchQuery.class))).thenReturn(books);

        // when
        var result = bookDownloadUsecase.execute(query);

        // then
        assertThat(result).isNotNull();
        var csvText = new String(result, StandardCharsets.UTF_8);
        assertThat(csvText).contains("ノルウェイの森");
        assertThat(csvText).contains("9783832185923");
    }

    @Test
    void execute_UTF8_BOM_先頭3バイトがEF_BB_BFであること() {
        // given
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);
        var books = List.of(new Book(
                1L,
                new Isbn("9783832185923"),
                "Test Book",
                1000,
                BookStatus.PUBLISHED,
                null,
                1,
                "Test Author",
                1,
                "Test Publisher",
                LocalDateTime.now(),
                null,
                null));
        when(repository.download(any(BookSearchQuery.class))).thenReturn(books);

        // when
        var result = bookDownloadUsecase.execute(query);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);
        assertThat(result[0]).isEqualTo((byte) 0xEF);
        assertThat(result[1]).isEqualTo((byte) 0xBB);
        assertThat(result[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    void execute_CSVヘッダー_正しいヘッダー行が含まれること() {
        // given
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);
        when(repository.download(any(BookSearchQuery.class))).thenReturn(List.of());

        // when
        var result = bookDownloadUsecase.execute(query);
        var csvText = new String(result, StandardCharsets.UTF_8);

        // then
        assertThat(csvText).contains("id");
        assertThat(csvText).contains("isbn");
        assertThat(csvText).contains("title");
        assertThat(csvText).contains("price");
        assertThat(csvText).contains("status");
        assertThat(csvText).contains("publishedAt");
    }

    @Test
    void execute_データ行_複数件の書籍データが正しくCSV変換されること() {
        // given
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);

        var books = List.of(
                new Book(
                        1L,
                        new Isbn("9783832185923"),
                        "ノルウェイの森",
                        1000,
                        BookStatus.PUBLISHED,
                        LocalDateTime.of(1987, 9, 4, 0, 0),
                        1,
                        "村上春樹",
                        1,
                        "講談社",
                        LocalDateTime.now(),
                        null,
                        null),
                new Book(
                        2L,
                        new Isbn("9784621303252"),
                        "Never Let Me Go",
                        1200,
                        BookStatus.OUT_OF_PRINT,
                        LocalDateTime.of(2005, 3, 3, 0, 0),
                        2,
                        "Kazuo Ishiguro",
                        2,
                        "新潮社",
                        LocalDateTime.now(),
                        null,
                        null));

        when(repository.download(any(BookSearchQuery.class))).thenReturn(books);

        // when
        var result = bookDownloadUsecase.execute(query);
        var csvText = new String(result, StandardCharsets.UTF_8);

        // then
        assertThat(csvText).contains("ノルウェイの森");
        assertThat(csvText).contains("Never Let Me Go");
        assertThat(csvText).contains("PUBLISHED");
        assertThat(csvText).contains("OUT_OF_PRINT");
    }

    @Test
    void execute_空結果_0件の書籍でも空CSVが返ること() {
        // given
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);
        when(repository.download(any(BookSearchQuery.class))).thenReturn(List.of());

        // when
        var result = bookDownloadUsecase.execute(query);
        var csvText = new String(result, StandardCharsets.UTF_8);

        // then
        assertThat(result).isNotEmpty();
        assertThat(csvText).contains("id");
    }
}
