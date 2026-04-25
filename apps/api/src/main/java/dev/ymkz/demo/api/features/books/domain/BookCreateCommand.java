package dev.ymkz.demo.api.features.books.domain;

import dev.ymkz.demo.core.domain.valueobject.Isbn;
import java.time.LocalDateTime;

public record BookCreateCommand(
        Isbn isbn,
        String title,
        Integer price,
        BookStatus status,
        LocalDateTime publishedAt,
        int authorId,
        int publisherId) {}
