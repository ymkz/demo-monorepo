package dev.ymkz.demo.api.presentation.dto;

import dev.ymkz.demo.api.domain.model.BookOrder;
import dev.ymkz.demo.api.domain.model.BookStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;

public record SearchBooksQueryParam(
        @Parameter(description = "ISBN-13:完全一致", example = "9784873115658") String isbn,
        @Parameter(description = "書籍タイトル:部分一致") String title,
        @Parameter(description = "価格:下限") @Min(0) Integer priceFrom,
        @Parameter(description = "価格:上限") @Min(0) Integer priceTo,
        @Parameter(description = "ステータス") List<BookStatus> status,
        @Parameter(description = "出版日時:ISO8601:開始", example = "2025-01-23T01:23:45.000Z") Instant publishedAtStart,
        @Parameter(description = "出版日時:ISO8601:終了", example = "2025-01-23T01:23:45.000Z") Instant publishedAtEnd,
        @Parameter(description = "並び順") @Schema(defaultValue = "-published_at") BookOrder order) {
    public SearchBooksQueryParam {
        if (order == null) {
            order = BookOrder.PUBLISHED_AT_DESC;
        }
    }
}
