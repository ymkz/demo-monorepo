package dev.ymkz.demo.api.domain.repository;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookCreateCommand;
import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.model.BookUpdateCommand;
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
