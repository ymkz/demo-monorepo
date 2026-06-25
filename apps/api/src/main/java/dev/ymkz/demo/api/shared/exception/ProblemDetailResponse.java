package dev.ymkz.demo.api.shared.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.List;

@Schema(description = "Problem Details for HTTP APIs")
public record ProblemDetailResponse(
        @Schema(description = "問題種別を識別するURI", example = "about:blank")
        URI type,

        @Schema(description = "問題の短い説明", example = "Bad Request")
        String title,

        @Schema(description = "HTTPステータスコード", example = "400")
        Integer status,

        @Schema(description = "問題の詳細説明", example = "リクエストボディの検証に失敗しました")
        String detail,

        @Schema(description = "問題が発生したリソースのURI", example = "/books")
        URI instance,

        @Schema(description = "アプリケーション固有のエラーコード", example = "dw1001")
        String errorCode,

        @ArraySchema(schema = @Schema(implementation = ValidationErrorResponse.class))
        List<ValidationErrorResponse> errors) {}
