package dev.ymkz.demo.api.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class WideEventLoggingFilter implements Filter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WideEventLoggingFilter() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(new ZonedDateTimeSerializer(DATE_TIME_FORMATTER));
        this.objectMapper.registerModule(javaTimeModule);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setTimeZone(java.util.TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));
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

        String requestId = EventsCollector.initialize(httpReq.getMethod(), httpReq.getRequestURI());
        MDC.put("requestId", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            WideEventLog finalLog = EventsCollector.finalizeLog(httpRes.getStatus());
            if (finalLog != null) {
                try {
                    // WideEventLogの各フィールドをトップレベルに展開（requestIdはMDCで出力されるので除外）
                    Map<String, Object> fields = objectMapper.convertValue(finalLog, Map.class);
                    fields.remove("requestId"); // MDCのrequestIdと重複するので除外
                    StructuredArgument[] args = fields.entrySet().stream()
                            .map(e -> StructuredArguments.value(e.getKey(), e.getValue()))
                            .toArray(StructuredArgument[]::new);
                    log.info("WIDE_EVENT", args);
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
