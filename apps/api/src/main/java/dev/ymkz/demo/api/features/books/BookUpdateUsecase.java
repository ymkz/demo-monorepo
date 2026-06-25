package dev.ymkz.demo.api.features.books;

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
