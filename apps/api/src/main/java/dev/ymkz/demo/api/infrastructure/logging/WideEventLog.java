package dev.ymkz.demo.api.infrastructure.logging;

import java.time.ZonedDateTime;
import java.util.List;

public record WideEventLog(
        String requestId,
        String method,
        String path,
        ZonedDateTime requestedAt,
        ZonedDateTime respondedAt,
        Long durationMs,
        Integer statusCode,
        List<Event> events,
        ErrorInfo error) {

    public record Event(ZonedDateTime timestamp, String type, String name, Object metadata) {}

    public record ErrorInfo(
            String type,
            String name,
            ZonedDateTime occurredAt,
            String errorType,
            String errorMessage,
            Object metadata) {}
}
