package dev.ymkz.demo.api.infrastructure.logging;

import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventsCollector {
    private static final int MAX_EVENTS = 100;
    private static final ThreadLocal<WideEventLog> holder = new ThreadLocal<>();

    public static void initialize(String method, String path, String userAgent) {
        WideEventLog eventLog = WideEventLog.builder()
                .requestId(UUID.randomUUID().toString())
                .method(method)
                .path(path)
                .startTime(Instant.now())
                .userAgent(userAgent)
                .build();
        holder.set(eventLog);
    }

    public static void record(String type, String name, Long durationMs, Object metadata) {
        WideEventLog eventLog = holder.get();
        if (eventLog == null) {
            return;
        }

        // 上限チェック: 超過時は古いものから削除
        if (eventLog.getEvents().size() >= MAX_EVENTS) {
            WideEventLog.Event removed = eventLog.getEvents().remove(0);
            log.warn(
                    "WideEventLog event limit exceeded. Dropping oldest event: type={}, name={}",
                    removed.getType(),
                    removed.getName());
        }

        eventLog.addEvent(WideEventLog.Event.builder()
                .timestamp(Instant.now())
                .type(type)
                .name(name)
                .durationMs(durationMs)
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
                .occurredAt(Instant.now())
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

        eventLog.setDurationMs(
                Instant.now().toEpochMilli() - eventLog.getStartTime().toEpochMilli());
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
