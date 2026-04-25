package dev.ymkz.demo.api.features.books.application;

import dev.ymkz.demo.api.features.books.domain.BookRepository;
import dev.ymkz.demo.api.features.books.domain.BookUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookUpdateUsecase {

    private final BookRepository repository;

    public void execute(BookUpdateCommand command) {
        repository.update(command);
    }
}
