package dev.ymkz.demo.api.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
@Slf4j
public class WideEventLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestId = EventsCollector.initialize(request.getMethod(), request.getRequestURI());
        MDC.put("requestId", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            try {
                WideEventLog eventLog = EventsCollector.finalizeLog(response.getStatus());
                try {
                    var args = new Object[] {
                        StructuredArguments.value("method", eventLog.method()),
                        StructuredArguments.value("path", eventLog.path()),
                        StructuredArguments.value("requestedAt", eventLog.requestedAt()),
                        StructuredArguments.value("respondedAt", eventLog.respondedAt()),
                        StructuredArguments.value("durationMs", eventLog.durationMs()),
                        StructuredArguments.value("statusCode", eventLog.statusCode()),
                        StructuredArguments.value("events", eventLog.events()),
                        StructuredArguments.value("error", eventLog.error())
                    };
                    if (eventLog.error() != null) {
                        log.error("WIDE_EVENT", args);
                    } else {
                        log.info("WIDE_EVENT", args);
                    }
                } catch (Exception e) {
                    log.error(
                            "Failed to serialize WideEventLog. requestId={} path={} status={} events={} error={}",
                            eventLog.requestId(),
                            eventLog.path(),
                            eventLog.statusCode(),
                            eventLog.events().size(),
                            eventLog.error() != null ? eventLog.error().exception() : "none",
                            e);
                }
            } finally {
                EventsCollector.clear();
                MDC.remove("requestId");
            }
        }
    }
}
