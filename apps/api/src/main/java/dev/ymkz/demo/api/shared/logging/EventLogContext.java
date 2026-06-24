package dev.ymkz.demo.api.shared.logging;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EventLogContext {
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    private final ZonedDateTime requestedAt = nowJst();
    private final Map<String, Object> fields = new LinkedHashMap<>();
    private final List<WideEventLog.Event> events = new ArrayList<>();
    private WideEventLog.ErrorInfo error;

    private static ZonedDateTime nowJst() {
        return ZonedDateTime.now(JST);
    }

    public void set(String key, Object value) {
        fields.put(key, value);
    }

    public void putIfAbsent(String key, Object value) {
        fields.putIfAbsent(key, value);
    }

    public void addEvent(String msg, Object metadata) {
        events.add(new WideEventLog.Event(WideEventLog.format(nowJst()), msg, metadata));
    }

    public void setError(Throwable ex, Object metadata) {
        error = new WideEventLog.ErrorInfo(
                WideEventLog.format(nowJst()), ex.getClass().getName(), ex.getMessage(), metadata);
    }

    public Snapshot snapshot() {
        return new Snapshot(
                requestedAt,
                nowJst(),
                Collections.unmodifiableMap(new LinkedHashMap<>(fields)),
                Collections.unmodifiableList(new ArrayList<>(events)),
                error);
    }

    public record Snapshot(
            ZonedDateTime requestedAt,
            ZonedDateTime respondedAt,
            Map<String, Object> fields,
            List<WideEventLog.Event> events,
            WideEventLog.ErrorInfo error) {}
}
