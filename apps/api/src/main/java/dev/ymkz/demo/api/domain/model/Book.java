package dev.ymkz.demo.api.domain.model;

import dev.ymkz.demo.core.domain.valueobject.Isbn;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record Book(
        long id,
        Isbn isbn,
        @NotBlank String title,
        @Nullable @PositiveOrZero Integer price,
        BookStatus status,
        @Nullable LocalDateTime publishedAt,
        int authorId,
        @NotBlank String authorName,
        int publisherId,
        @NotBlank String publisherName,
        LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt,
        @Nullable LocalDateTime deletedAt) {}
