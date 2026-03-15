package dev.ymkz.demo.api.infrastructure.logging;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventsCollector {
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
    private static final ThreadLocal<Context> holder = new ThreadLocal<>();

    private record Context(
            String requestId,
            String method,
            String path,
            ZonedDateTime requestedAt,
            List<WideEventLog.Event> events,
            WideEventLog.ErrorInfo error) {}

    private static ZonedDateTime nowJst() {
        return ZonedDateTime.now(JST);
    }

    public static String initialize(String method, String path) {
        String requestId = UUID.randomUUID().toString();
        Context ctx = new Context(requestId, method, path, nowJst(), new ArrayList<>(), null);
        holder.set(ctx);
        return requestId;
    }

    public static void record(String msg, Object metadata) {
        Context ctx = holder.get();
        if (ctx == null) {
            return;
        }

        ctx.events.add(new WideEventLog.Event(nowJst(), msg, metadata));
    }

    public static void setError(Exception ex, Object metadata) {
        Context ctx = holder.get();
        if (ctx == null) {
            return;
        }

        WideEventLog.ErrorInfo errorInfo =
                new WideEventLog.ErrorInfo(nowJst(), ex.getClass().getSimpleName(), ex.getMessage(), metadata);

        List<WideEventLog.Event> newEvents = new ArrayList<>(ctx.events);
        holder.set(new Context(ctx.requestId, ctx.method, ctx.path, ctx.requestedAt, newEvents, errorInfo));
    }

    public static WideEventLog finalizeLog(int statusCode) {
        Context ctx = holder.get();
        if (ctx == null) {
            return null;
        }

        ZonedDateTime now = nowJst();
        long durationMs =
                now.toInstant().toEpochMilli() - ctx.requestedAt.toInstant().toEpochMilli();

        return new WideEventLog(
                ctx.requestId,
                ctx.method,
                ctx.path,
                ctx.requestedAt,
                now,
                durationMs,
                statusCode,
                ctx.events,
                ctx.error);
    }

    public static String getRequestId() {
        Context ctx = holder.get();
        return ctx != null ? ctx.requestId : "";
    }

    public static void clear() {
        holder.remove();
    }
}
