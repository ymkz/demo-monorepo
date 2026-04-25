package dev.ymkz.demo.api.features.books.presentation.dto;

import dev.ymkz.demo.api.features.books.domain.BookStatus;
import dev.ymkz.demo.api.features.books.domain.BookUpdateCommand;
import dev.ymkz.demo.core.domain.valueobject.Isbn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record UpdateBookBody(
        @Schema(description = "ISBN-13", example = "9784873115658")
        String isbn,

        @Schema(description = "書籍タイトル", example = "リーダブルコード")
        String title,

        @Schema(description = "価格", example = "2640") @Min(0)
        Integer price,

        @Schema(description = "ステータス", example = "PUBLISHED")
        BookStatus status,

        @Schema(description = "出版日時:ISO8601", example = "2025-01-23T01:23:45.000Z")
        LocalDateTime publishedAt,

        @Schema(description = "著者ID", example = "1") Integer authorId,

        @Schema(description = "出版社ID", example = "1") Integer publisherId) {
    public BookUpdateCommand toCommand(long id) {
        return new BookUpdateCommand(id, Isbn.of(isbn), title, price, status, publishedAt, authorId, publisherId);
    }
}
