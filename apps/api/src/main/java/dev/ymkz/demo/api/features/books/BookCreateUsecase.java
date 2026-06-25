package dev.ymkz.demo.api.features.books;

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
