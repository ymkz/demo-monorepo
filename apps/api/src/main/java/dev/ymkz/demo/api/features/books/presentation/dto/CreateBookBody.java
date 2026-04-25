package dev.ymkz.demo.api.features.books.presentation.dto;

import dev.ymkz.demo.api.features.books.domain.BookCreateCommand;
import dev.ymkz.demo.api.features.books.domain.BookStatus;
import dev.ymkz.demo.core.domain.valueobject.Isbn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateBookBody(
        @Schema(description = "ISBN-13", example = "9784873115658") @NotBlank
        String isbn,

        @Schema(description = "書籍タイトル", example = "リーダブルコード") @NotBlank
        String title,

        @Schema(description = "価格", example = "2640") @Min(0)
        Integer price,

        @Schema(description = "ステータス", example = "PUBLISHED") @NotNull
        BookStatus status,

        @Schema(description = "出版日時:ISO8601", example = "2025-01-23T01:23:45.000Z")
        LocalDateTime publishedAt,

        @Schema(description = "著者ID", example = "1") @NotNull
        Integer authorId,

        @Schema(description = "出版社ID", example = "1") @NotNull
        Integer publisherId) {
    public BookCreateCommand toCommand() {
        return new BookCreateCommand(Isbn.of(isbn), title, price, status, publishedAt, authorId, publisherId);
    }
}
