package dev.ymkz.demo.api.application.usecase;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.repository.BookRepository;
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
