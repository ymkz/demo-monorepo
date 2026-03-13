package dev.ymkz.demo.api.infrastructure.logging;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WideEventLog {
    private String requestId;
    private String method;
    private String path;
    private ZonedDateTime requestedAt;
    private Long durationMs;
    private Integer statusCode;
    private String userAgent;

    @Builder.Default
    private List<Event> events = new ArrayList<>();

    private ErrorInfo error;

    @Data
    @Builder
    public static class Event {
        private ZonedDateTime timestamp;
        private String type;
        private String name;
        private Long durationMs;
        private Object metadata;
    }

    @Data
    @Builder
    public static class ErrorInfo {
        private String type;
        private String name;
        private ZonedDateTime occurredAt;
        private String errorType;
        private String errorMessage;
        private Object metadata;
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }
}
