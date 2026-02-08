package dev.ymkz.demo.api.infrastructure.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.model.BookStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TODO: findById/create/update/deleteメソッドが実装されたらテストを追加
@ExtendWith(MockitoExtension.class)
class BookDatasourceTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookDatasource bookDatasource;

    @Test
    void search_countとlistで正しいPaginatedが構築されること() {
        var query = new BookSearchQuery(null, null, null, null, null, null, 0, 20);

        var entities = List.of(new BookEntity(
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
                LocalDateTime.now(),
                null,
                null));

        when(bookMapper.count(any(BookSearchQuery.class))).thenReturn(1);
        when(bookMapper.list(any(BookSearchQuery.class))).thenReturn(entities);

        var result = bookDatasource.search(query);

        assertThat(result).isNotNull();
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("ノルウェイの森");
        assertThat(result.offset()).isEqualTo(0);
        assertThat(result.limit()).isEqualTo(20);

        verify(bookMapper, times(1)).count(any(BookSearchQuery.class));
        verify(bookMapper, times(1)).list(any(BookSearchQuery.class));
    }

    @Test
    void search_0件の場合_emptyPaginatedが返ること() {
        var query = new BookSearchQuery(null, null, null, null, null, null, 0, 20);

        when(bookMapper.count(any(BookSearchQuery.class))).thenReturn(0);
        when(bookMapper.list(any(BookSearchQuery.class))).thenReturn(List.of());

        var result = bookDatasource.search(query);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    @Test
    void download_list結果をBookリストに変換すること() {
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);

        var entities = List.of(
                new BookEntity(
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
                        LocalDateTime.now(),
                        null,
                        null),
                new BookEntity(
                        2L,
                        "9784621303252",
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

        when(bookMapper.list(any(BookSearchQuery.class))).thenReturn(entities);

        var result = bookDatasource.download(query);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("ノルウェイの森");
        assertThat(result.get(1).title()).isEqualTo("Never Let Me Go");

        verify(bookMapper, times(1)).list(any(BookSearchQuery.class));
    }

    @Test
    void download_0件の場合_emptyListが返ること() {
        var query = new BookSearchQuery(null, null, null, null, null, null, null, null);

        when(bookMapper.list(any(BookSearchQuery.class))).thenReturn(List.of());

        var result = bookDatasource.download(query);

        assertThat(result).isEmpty();
    }
}
