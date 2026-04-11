package dev.ymkz.demo.api.features.books.domain;

import dev.ymkz.demo.core.domain.valueobject.Paginated;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Paginated<Book> search(BookSearchQuery query);

    List<Book> download(BookSearchQuery query);

    Optional<Book> findById(long id);

    void create(BookCreateCommand book);

    void update(BookUpdateCommand book);

    void delete(long id);
}
