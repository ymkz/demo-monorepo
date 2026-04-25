package dev.ymkz.demo.api.features.books.infrastructure;

import dev.ymkz.demo.api.features.books.domain.Book;
import dev.ymkz.demo.api.features.books.domain.BookCreateCommand;
import dev.ymkz.demo.api.features.books.domain.BookRepository;
import dev.ymkz.demo.api.features.books.domain.BookSearchQuery;
import dev.ymkz.demo.api.features.books.domain.BookUpdateCommand;
import dev.ymkz.demo.core.domain.valueobject.Paginated;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookDatasource implements BookRepository {

    private final BookMapper bookMapper;

    @Override
    public Paginated<Book> search(BookSearchQuery query) {
        var total = bookMapper.count(query);
        var content = bookMapper.list(query).stream().map(BookEntity::toBook).toList();
        return new Paginated<>(content, total, query.offset(), query.limit());
    }

    @Override
    public List<Book> download(BookSearchQuery query) {
        return bookMapper.list(query).stream().map(BookEntity::toBook).toList();
    }

    @Override
    public Optional<Book> findById(long id) {
        return bookMapper.findById(id).map(BookEntity::toBook);
    }

    @Override
    public void create(BookCreateCommand book) {
        bookMapper.create(book);
    }

    @Override
    public void update(BookUpdateCommand book) {
        if (bookMapper.update(book) == 0) {
            throw new NoSuchElementException("Book not found: " + book.id());
        }
    }

    @Override
    public void delete(long id) {
        if (bookMapper.delete(id) == 0) {
            throw new NoSuchElementException("Book not found: " + id);
        }
    }
}
