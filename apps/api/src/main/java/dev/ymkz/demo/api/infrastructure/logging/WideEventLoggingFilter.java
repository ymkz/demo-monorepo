package dev.ymkz.demo.api.infrastructure.logging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class WideEventLoggingFilter implements Filter {

    private final ObjectMapper objectMapper;

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
                // WideEventLogの各フィールドをトップレベルに展開（requestIdはMDCで出力されるので除外）
                Map<String, Object> fields = objectMapper.convertValue(eventLog, new TypeReference<>() {});
                fields.remove("requestId"); // MDCのrequestIdと重複するので除外
                var args = fields.entrySet().stream()
                        .map(e -> StructuredArguments.value(e.getKey(), e.getValue()))
                        .toArray(StructuredArgument[]::new);

                // エラー有無でログレベルを切り替え
                if (eventLog.error() != null) {
                    log.error("WIDE_EVENT", (Object[]) args);
                } else {
                    log.info("WIDE_EVENT", (Object[]) args);
                }
            } catch (Exception e) {
                // フォールバック: 最低限の情報は必ず出力
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
