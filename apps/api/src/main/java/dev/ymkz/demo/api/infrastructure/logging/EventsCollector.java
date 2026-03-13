package dev.ymkz.demo.api.infrastructure.logging;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class EventsCollector {
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
    private static final ThreadLocal<WideEventLog> holder = new ThreadLocal<>();

    public static String initialize(String method, String path) {
        String requestId = UUID.randomUUID().toString();
        WideEventLog eventLog = WideEventLog.builder()
                .requestId(requestId)
                .method(method)
                .path(path)
                .requestedAt(ZonedDateTime.now(JST))
                .build();
        holder.set(eventLog);
        return requestId;
    }

    public static void record(String type, String name, Object metadata) {
        WideEventLog eventLog = holder.get();
        if (eventLog == null) {
            return;
        }

        eventLog.addEvent(WideEventLog.Event.builder()
                .timestamp(ZonedDateTime.now(JST))
                .type(type)
                .name(name)
                .metadata(metadata)
                .build());
    }

    public static void setError(String type, String name, Exception ex, Object metadata) {
        WideEventLog eventLog = holder.get();
        if (eventLog == null) {
            return;
        }

        WideEventLog.ErrorInfo errorInfo = WideEventLog.ErrorInfo.builder()
                .type(type)
                .name(name)
                .occurredAt(ZonedDateTime.now(JST))
                .errorType(ex.getClass().getSimpleName())
                .errorMessage(ex.getMessage())
                .metadata(metadata)
                .build();

        eventLog.setError(errorInfo);
    }

    public static WideEventLog finalizeLog(int statusCode) {
        WideEventLog eventLog = holder.get();
        if (eventLog == null) {
            return null;
        }

        ZonedDateTime now = ZonedDateTime.now(JST);
        eventLog.setRespondedAt(now);
        eventLog.setDurationMs(now.toInstant().toEpochMilli()
                - eventLog.getRequestedAt().toInstant().toEpochMilli());
        eventLog.setStatusCode(statusCode);

        return eventLog;
    }

    public static String getRequestId() {
        WideEventLog eventLog = holder.get();
        return eventLog != null ? eventLog.getRequestId() : null;
    }

    public static void clear() {
        holder.remove();
    }
}
