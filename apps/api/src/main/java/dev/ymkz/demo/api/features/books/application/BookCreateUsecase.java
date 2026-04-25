package dev.ymkz.demo.api.features.books.application;

import dev.ymkz.demo.api.features.books.domain.BookCreateCommand;
import dev.ymkz.demo.api.features.books.domain.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookCreateUsecase {

    private final BookRepository repository;

    public void execute(BookCreateCommand command) {
        repository.create(command);
    }
}
