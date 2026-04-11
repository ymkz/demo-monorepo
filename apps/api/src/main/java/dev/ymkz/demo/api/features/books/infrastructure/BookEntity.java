package dev.ymkz.demo.api.features.books.infrastructure;

import dev.ymkz.demo.api.features.books.domain.Book;
import dev.ymkz.demo.api.features.books.domain.BookStatus;
import dev.ymkz.demo.core.domain.valueobject.Isbn;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;

public record BookEntity(
        @Nonnull long id,
        @Nonnull String isbn,
        @Nonnull String title,
        @Nullable Integer price,
        @Nonnull BookStatus status,
        @Nullable LocalDateTime publishedAt,
        @Nonnull int authorId,
        @Nonnull String authorName,
        @Nonnull int publisherId,
        @Nonnull String publisherName,
        @Nonnull LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt,
        @Nullable LocalDateTime deletedAt) {
    public Book toBook() {
        return new Book(
                id,
                new Isbn(isbn),
                title,
                price,
                status,
                publishedAt,
                authorId,
                authorName,
                publisherId,
                publisherName,
                createdAt,
                updatedAt,
                deletedAt);
    }
}
