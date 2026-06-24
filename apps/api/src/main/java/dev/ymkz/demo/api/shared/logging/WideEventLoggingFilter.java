package dev.ymkz.demo.api.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class WideEventLoggingFilter extends OncePerRequestFilter {

    private final ObjectProvider<EventLogContext> eventLogProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        MDC.put("http.request.id", requestId);
        Throwable error = null;

        try {
            chain.doFilter(request, response);
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            try {
                EventLogContext eventLog = eventLogProvider.getIfAvailable();
                if (eventLog != null && error != null) {
                    eventLog.setError(error, null);
                }

                WideEventLog dto = toWideEventLog(requestId, request, response, eventLog);
                try {
                    var args = new Object[] {
                        StructuredArguments.value("http.request.method", dto.method()),
                        StructuredArguments.value("url.path", dto.path()),
                        StructuredArguments.value("event.start", dto.requestedAt()),
                        StructuredArguments.value("event.end", dto.respondedAt()),
                        StructuredArguments.value("event.duration", dto.durationMs() * 1_000_000),
                        StructuredArguments.value("http.response.status_code", dto.statusCode()),
                        StructuredArguments.value("events", dto.events()),
                        StructuredArguments.value("error", dto.error()),
                        StructuredArguments.entries(
                                eventLog != null ? eventLog.snapshot().fields() : Map.of())
                    };
                    if (dto.error() != null) {
                        log.error("response_error", args);
                    } else {
                        log.info("response_success", args);
                    }
                } catch (Exception ex) {
                    log.error(
                            "Failed to serialize WideEventLog. requestId={} path={} status={} events={} error={}",
                            dto.requestId(),
                            dto.path(),
                            dto.statusCode(),
                            dto.events().size(),
                            dto.error() != null ? dto.error().exception() : "none",
                            ex);
                }
            } finally {
                MDC.remove("http.request.id");
            }
        }
    }

    private WideEventLog toWideEventLog(
            String requestId, HttpServletRequest request, HttpServletResponse response, EventLogContext eventLog) {
        EventLogContext.Snapshot snapshot = eventLog != null ? eventLog.snapshot() : null;
        ZonedDateTime requestedAt = snapshot != null ? snapshot.requestedAt() : ZonedDateTime.now();
        ZonedDateTime respondedAt = snapshot != null ? snapshot.respondedAt() : ZonedDateTime.now();
        long durationMs =
                respondedAt.toInstant().toEpochMilli() - requestedAt.toInstant().toEpochMilli();

        return new WideEventLog(
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                WideEventLog.format(requestedAt),
                WideEventLog.format(respondedAt),
                durationMs,
                response.getStatus(),
                snapshot != null ? snapshot.events() : List.of(),
                snapshot != null ? snapshot.error() : null);
    }
}
