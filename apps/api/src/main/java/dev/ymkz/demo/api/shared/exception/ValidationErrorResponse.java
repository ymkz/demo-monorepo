package dev.ymkz.demo.api.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "バリデーションエラーの詳細")
public record ValidationErrorResponse(
        @Schema(description = "エラーが発生したフィールド", example = "isbn")
        String field,

        @Schema(description = "エラーメッセージ", example = "空白は許可されていません")
        String message) {}
