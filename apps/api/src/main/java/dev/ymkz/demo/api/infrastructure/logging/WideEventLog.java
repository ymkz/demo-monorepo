package dev.ymkz.demo.api.infrastructure.logging;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record WideEventLog(
        String requestId,
        String method,
        String path,
        String requestedAt,
        String respondedAt,
        Long durationMs,
        Integer statusCode,
        List<Event> events,
        ErrorInfo error) {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String format(ZonedDateTime dt) {
        return dt != null ? dt.format(FORMATTER) : null;
    }

    public record Event(String timestamp, String msg, Object metadata) {}

    public record ErrorInfo(String occurredAt, String exception, String msg, Object metadata) {}
}
