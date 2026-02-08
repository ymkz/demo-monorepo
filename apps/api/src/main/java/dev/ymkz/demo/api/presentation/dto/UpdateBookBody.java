package dev.ymkz.demo.api.presentation.dto;

import dev.ymkz.demo.api.domain.model.BookStatus;
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
        LocalDateTime publishedAt) {}
