package dev.ymkz.demo.api.features.books.domain;

import dev.ymkz.demo.core.domain.valueobject.Isbn;
import java.time.LocalDateTime;

public record BookUpdateCommand(
        long id,
        Isbn isbn,
        String title,
        Integer price,
        BookStatus status,
        LocalDateTime publishedAt,
        Integer authorId,
        Integer publisherId) {}
