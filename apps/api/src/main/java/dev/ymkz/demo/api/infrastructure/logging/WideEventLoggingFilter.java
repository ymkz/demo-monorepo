package dev.ymkz.demo.api.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class WideEventLoggingFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WideEventLoggingFilter() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String requestId = EventsCollector.initialize(
                httpReq.getMethod(), httpReq.getRequestURI(), httpReq.getHeader("User-Agent"));
        MDC.put("requestId", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            WideEventLog finalLog = EventsCollector.finalizeLog(httpRes.getStatus());
            if (finalLog != null) {
                try {
                    String json = objectMapper.writeValueAsString(finalLog);
                    log.info("WIDE_EVENT {}", json);
                } catch (Exception e) {
                    // フォールバック: 最低限の情報は必ず出力
                    log.error(
                            "Failed to serialize WideEventLog. requestId={} path={} status={} events={} error={}",
                            finalLog.getRequestId(),
                            finalLog.getPath(),
                            finalLog.getStatusCode(),
                            finalLog.getEvents().size(),
                            finalLog.getError() != null ? finalLog.getError().getErrorType() : "none",
                            e);
                }
            }
            EventsCollector.clear();
            MDC.remove("requestId");
        }
    }
}
