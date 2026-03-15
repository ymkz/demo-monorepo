package dev.ymkz.demo.api.infrastructure.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class WideEventLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String requestId = EventsCollector.initialize(httpReq.getMethod(), httpReq.getRequestURI());
        MDC.put("requestId", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            WideEventLog eventLog = EventsCollector.finalizeLog(httpRes.getStatus());
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
            EventsCollector.clear();
            MDC.remove("requestId");
        }
    }
}
