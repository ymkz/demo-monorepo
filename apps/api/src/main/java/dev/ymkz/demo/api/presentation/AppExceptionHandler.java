package dev.ymkz.demo.api.presentation;

import dev.ymkz.demo.api.infrastructure.logging.EventsCollector;
import dev.ymkz.demo.api.presentation.dto.ErrorResponse;
import dev.ymkz.demo.core.domain.event.AppEvent;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
@Slf4j
public class AppExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.info("Handling ConstraintViolationException: {}", ex.getMessage());
        return createErrorResponse(AppEvent.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.info("Handling HandlerMethodValidationException: {}", ex.getMessage());
        return createErrorResponse(AppEvent.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.info("Handling IllegalArgumentException: {}", ex.getMessage());
        return createErrorResponse(AppEvent.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<ErrorResponse> handleMyBatisException(MyBatisSystemException ex) {
        return createErrorResponse(AppEvent.DATABASE_MYBATIS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherException(Exception ex) {
        log.info("Handling Exception: {} - {}", ex.getClass().getName(), ex.getMessage());
        return createErrorResponse(AppEvent.UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(AppEvent event, HttpStatus status, Exception ex) {
        String requestId = EventsCollector.getRequestId();
        log.error("Error requestId={} event={}", requestId, event, ex);

        String errorType =
                switch (event) {
                    case VALIDATION_FAILED -> "validation";
                    case DATABASE_MYBATIS_ERROR -> "db_query";
                    default -> "error";
                };

        EventsCollector.setError(errorType, event.name(), ex, null);

        return ResponseEntity.status(status).body(ErrorResponse.of(event));
    }
}
