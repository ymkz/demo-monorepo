package dev.ymkz.demo.api.features.books.application;

import dev.ymkz.demo.api.features.books.domain.Book;
import dev.ymkz.demo.api.features.books.domain.BookRepository;
import dev.ymkz.demo.api.features.books.domain.BookSearchQuery;
import dev.ymkz.demo.core.domain.valueobject.Paginated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookSearchUsecase {

    private final BookRepository repository;

    public Paginated<Book> execute(BookSearchQuery query) {
        return repository.search(query);
    }
}
