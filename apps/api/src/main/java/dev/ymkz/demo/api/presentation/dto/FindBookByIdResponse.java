package dev.ymkz.demo.api.presentation.dto;

import dev.ymkz.demo.api.domain.model.Book;
import dev.ymkz.demo.api.domain.model.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record FindBookByIdResponse(
        @Schema(description = "書籍ID", example = "1") @NotNull
        long id,

        @Schema(description = "ISBN-13", example = "9784873115658") @NotNull
        String isbn,

        @Schema(description = "タイトル", example = "リーダブルコード") @NotNull
        String title,

        @Schema(description = "価格", example = "2640") Integer price,

        @Schema(description = "ステータス", example = "PUBLISHED") @NotNull
        BookStatus status,

        @Schema(description = "出版日時:ISO8601", example = "2025-01-23T01:23:45.000Z")
        LocalDateTime publishedAt,

        @Schema(description = "著者ID", example = "1") int authorId,

        @Schema(description = "著者名", example = "Dustin Boswell")
        String authorName,

        @Schema(description = "出版社ID", example = "1") int publisherId,
        @Schema(description = "出版社名", example = "O'Reilly") String publisherName) {
    public static FindBookByIdResponse of(Book book) {
        return new FindBookByIdResponse(
                book.id(),
                book.isbn().value(),
                book.title(),
                book.price(),
                book.status(),
                book.publishedAt(),
                book.authorId(),
                book.authorName(),
                book.publisherId(),
                book.publisherName());
    }
}
