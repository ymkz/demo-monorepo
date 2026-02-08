package dev.ymkz.demo.api.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.model.BookStatus;
import dev.ymkz.demo.api.domain.repository.BookRepository;
import dev.ymkz.demo.core.domain.valueobject.Isbn;
import dev.ymkz.demo.core.domain.valueobject.Paginated;
import dev.ymkz.demo.core.domain.valueobject.RangeInteger;
import dev.ymkz.demo.core.domain.valueobject.RangeTime;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookSearchUsecaseTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookSearchUsecase bookSearchUsecase;

    @Test
    void execute_正常系_検索結果が返ること() {
        // given
        var query = new BookSearchQuery(
                Isbn.of("9783832185923"),
                "ノルウェイの森",
                RangeInteger.of(1000, 2000),
                List.of(BookStatus.PUBLISHED),
                RangeTime.of(
                        LocalDateTime.of(1980, 1, 1, 0, 0)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant(),
                        LocalDateTime.of(1990, 1, 1, 0, 0)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant()),
                null,
                0,
                20);

        var expectedBooks = List.of(new Book(
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

        Paginated<Book> expectedPaginated = new Paginated<>(expectedBooks, 1, 0, 20);
        when(repository.search(any(BookSearchQuery.class))).thenReturn(expectedPaginated);

        // when
        var result = bookSearchUsecase.execute(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items().get(0).title()).isEqualTo("ノルウェイの森");
    }

    @Test
    void execute_境界値_offset0_limit1() {
        var query = new BookSearchQuery(null, null, null, null, null, null, 0, 1);
        @SuppressWarnings("unchecked")
        Paginated<Book> expectedPaginated = new Paginated<>(List.of(), 0, 0, 1);
        when(repository.search(any(BookSearchQuery.class))).thenReturn(expectedPaginated);

        var result = bookSearchUsecase.execute(query);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    @Test
    void execute_境界値_offsetLarge_limitMax() {
        var query = new BookSearchQuery(null, null, null, null, null, null, 10000, 100);
        @SuppressWarnings("unchecked")
        Paginated<Book> expectedPaginated = new Paginated<>(List.of(), 0, 10000, 100);
        when(repository.search(any(BookSearchQuery.class))).thenReturn(expectedPaginated);

        var result = bookSearchUsecase.execute(query);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    @Test
    void execute_空結果_検索条件に合致する書籍が0件() {
        var query = new BookSearchQuery(
                null, "存在しないタイトル", RangeInteger.of(999999, 999999), List.of(BookStatus.UNPUBLISHED), null, null, 0, 20);

        @SuppressWarnings("unchecked")
        Paginated<Book> expectedPaginated = new Paginated<>(List.of(), 0, 0, 20);
        when(repository.search(any(BookSearchQuery.class))).thenReturn(expectedPaginated);

        var result = bookSearchUsecase.execute(query);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }
}
