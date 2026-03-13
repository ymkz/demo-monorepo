package dev.ymkz.demo.api.infrastructure.logging;

import java.time.Instant;
import java.util.UUID;

public class EventsCollector {
    private static final int MAX_EVENTS = 100;
    private static final ThreadLocal<WideEventLog> holder = new ThreadLocal<>();

    public static void initialize(String method, String path, String userAgent) {
        WideEventLog log = WideEventLog.builder()
                .requestId(UUID.randomUUID().toString())
                .method(method)
                .path(path)
                .startTime(Instant.now())
                .userAgent(userAgent)
                .build();
        holder.set(log);
    }

    public static void record(String type, String name, Long durationMs, Object metadata) {
        WideEventLog log = holder.get();
        if (log == null) {
            return;
        }

        // 上限チェック: 超過時は古いものから削除
        if (log.getEvents().size() >= MAX_EVENTS) {
            log.getEvents().remove(0);
        }

        log.addEvent(WideEventLog.Event.builder()
                .timestamp(Instant.now())
                .type(type)
                .name(name)
                .durationMs(durationMs)
                .metadata(metadata)
                .build());
    }

    public static void setError(String type, String name, Exception ex, Object metadata) {
        WideEventLog log = holder.get();
        if (log == null) {
            return;
        }

        WideEventLog.ErrorInfo errorInfo = WideEventLog.ErrorInfo.builder()
                .type(type)
                .name(name)
                .occurredAt(Instant.now())
                .errorType(ex.getClass().getSimpleName())
                .errorMessage(ex.getMessage())
                .metadata(metadata)
                .build();

        log.setError(errorInfo);
    }

    public static WideEventLog finalizeLog(int statusCode) {
        WideEventLog log = holder.get();
        if (log == null) {
            return null;
        }

        log.setDurationMs(Instant.now().toEpochMilli() - log.getStartTime().toEpochMilli());
        log.setStatusCode(statusCode);

        return log;
    }

    public static String getRequestId() {
        WideEventLog log = holder.get();
        return log != null ? log.getRequestId() : null;
    }

    public static void clear() {
        holder.remove();
    }
}
