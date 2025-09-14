package dev.ymkz.demo.api.infrastructure.datasource;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookCreateCommand;
import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.model.BookUpdateCommand;
import dev.ymkz.demo.api.domain.repository.BookRepository;
import dev.ymkz.demo.core.domain.valueobject.Paginated;
import java.util.List;
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
        var content = bookMapper.list(query).stream().map(BookEntity::toBook).toList();
        return content;
    }

    @Override
    public Optional<Book> findById(long id) {
        return null;
    }

    @Override
    public void create(BookCreateCommand book) {
        return;
    }

    @Override
    public void update(BookUpdateCommand book) {
        return;
    }

    @Override
    public void delete(long id) {
        return;
    }
}
