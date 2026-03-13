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

    public static String initialize(String method, String path) {
        String requestId = UUID.randomUUID().toString();
        Context ctx = new Context(requestId, method, path, ZonedDateTime.now(JST), new ArrayList<>(), null);
        holder.set(ctx);
        return requestId;
    }

    public static void record(String type, String name, Object metadata) {
        Context ctx = holder.get();
        if (ctx == null) {
            return;
        }

        ctx.events.add(new WideEventLog.Event(ZonedDateTime.now(JST), type, name, metadata));
    }

    public static void setError(Exception ex, Object metadata) {
        Context ctx = holder.get();
        if (ctx == null) {
            return;
        }

        WideEventLog.ErrorInfo errorInfo = new WideEventLog.ErrorInfo(
                ZonedDateTime.now(JST), ex.getClass().getSimpleName(), ex.getMessage(), metadata);

        holder.set(new Context(ctx.requestId, ctx.method, ctx.path, ctx.requestedAt, ctx.events, errorInfo));
    }

    public static WideEventLog finalizeLog(int statusCode) {
        Context ctx = holder.get();
        if (ctx == null) {
            return null;
        }

        ZonedDateTime now = ZonedDateTime.now(JST);
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
        return ctx != null ? ctx.requestId : null;
    }

    public static void clear() {
        holder.remove();
    }
}
