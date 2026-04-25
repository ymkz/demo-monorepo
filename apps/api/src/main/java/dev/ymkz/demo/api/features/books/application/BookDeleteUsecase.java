package dev.ymkz.demo.api.features.books.application;

import dev.ymkz.demo.api.features.books.domain.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookDeleteUsecase {

    private final BookRepository repository;

    public void execute(long id) {
        repository.delete(id);
    }
}
