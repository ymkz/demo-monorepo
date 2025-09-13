package dev.ymkz.demo.api.infrastructure.datasource;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookStatus;
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
    public static Book toBook(BookEntity entity) {
        return new Book(
                entity.id,
                new Isbn(entity.isbn),
                entity.title,
                entity.price,
                entity.status,
                entity.publishedAt,
                entity.authorId,
                entity.authorName,
                entity.publisherId,
                entity.publisherName,
                entity.createdAt,
                entity.updatedAt,
                entity.deletedAt);
    }
}
