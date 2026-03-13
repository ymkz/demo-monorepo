package dev.ymkz.demo.api.infrastructure.logging;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventsCollector {
    private static final int MAX_EVENTS = 100;
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
    private static final ThreadLocal<WideEventLog> holder = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> warned = new ThreadLocal<>();

    public static String initialize(String method, String path) {
        String requestId = UUID.randomUUID().toString();
        WideEventLog eventLog = WideEventLog.builder()
                .requestId(requestId)
                .method(method)
                .path(path)
                .requestedAt(ZonedDateTime.now(JST))
                .build();
        holder.set(eventLog);
        warned.set(false);
        return requestId;
    }

    public static void record(String type, String name, Object metadata) {
        WideEventLog eventLog = holder.get();
        if (eventLog == null) {
            return;
        }

        // 上限チェック: 超過時は古いものから削除（WARNは1リクエストにつき1回のみ）
        if (eventLog.getEvents().size() >= MAX_EVENTS) {
            WideEventLog.Event removed = eventLog.getEvents().remove(0);
            if (!Boolean.TRUE.equals(warned.get())) {
                warned.set(true);
                log.warn(
                        "WideEventLog event limit exceeded. Dropping oldest event: type={}, name={}",
                        removed.getType(),
                        removed.getName());
            }
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
        warned.remove();
    }
}
