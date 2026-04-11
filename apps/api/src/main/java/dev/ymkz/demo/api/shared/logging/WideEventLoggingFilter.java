package dev.ymkz.demo.api.shared.logging;

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
                WideEventLog dto = EventsCollector.finalize(response.getStatus());
                try {
                    var args = new Object[] {
                        StructuredArguments.value("method", dto.method()),
                        StructuredArguments.value("path", dto.path()),
                        StructuredArguments.value("requestedAt", dto.requestedAt()),
                        StructuredArguments.value("respondedAt", dto.respondedAt()),
                        StructuredArguments.value("durationMs", dto.durationMs()),
                        StructuredArguments.value("statusCode", dto.statusCode()),
                        StructuredArguments.value("events", dto.events()),
                        StructuredArguments.value("error", dto.error())
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
                EventsCollector.clear();
                MDC.remove("requestId");
            }
        }
    }
}
