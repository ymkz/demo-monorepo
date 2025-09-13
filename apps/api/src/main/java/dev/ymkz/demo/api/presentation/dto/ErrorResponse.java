package dev.ymkz.demo.api.presentation.dto;

import dev.ymkz.demo.core.domain.event.AppEvent;

public record ErrorResponse(String code, String message) {
    public static ErrorResponse of(AppEvent event) {
        return new ErrorResponse(event.code(), event.message());
    }
}
