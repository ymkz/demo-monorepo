package dev.ymkz.demo.api.presentation;

import dev.ymkz.demo.api.infrastructure.logging.EventsCollector;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String requestId = EventsCollector.getRequestId();
        log.error("Validation error requestId={}", requestId, ex);
        EventsCollector.setError("validation", "VALIDATION_FAILED", ex, null);

        // ProblemDetails形式を維持
        return super.handleHandlerMethodValidationException(ex, headers, status, request);
    }

    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<Object> handleMyBatisException(MyBatisSystemException ex, WebRequest request) {
        String requestId = EventsCollector.getRequestId();
        log.error("Database error requestId={}", requestId, ex);
        EventsCollector.setError("db_query", "DATABASE_MYBATIS_ERROR", ex, null);

        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
