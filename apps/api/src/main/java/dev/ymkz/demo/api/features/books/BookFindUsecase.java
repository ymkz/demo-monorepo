package dev.ymkz.demo.api.features.books;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookFindUsecase {

    private final BookRepository repository;

    public Book execute(long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Book not found: " + id));
    }
}
