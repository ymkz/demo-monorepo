package dev.ymkz.demo.api.infrastructure.logging;

import java.time.Instant;
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
    private Instant startTime;
    private Long durationMs;
    private Integer statusCode;
    private String userAgent;

    @Builder.Default
    private List<Event> events = new ArrayList<>();

    private ErrorInfo error;

    @Data
    @Builder
    public static class Event {
        private Instant timestamp;
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
        private Instant occurredAt;
        private String errorType;
        private String errorMessage;
        private Object metadata;
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }
}
