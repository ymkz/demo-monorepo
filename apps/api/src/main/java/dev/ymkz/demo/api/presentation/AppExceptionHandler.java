package dev.ymkz.demo.api.presentation;

import dev.ymkz.demo.api.presentation.dto.ErrorResponse;
import dev.ymkz.demo.core.domain.event.AppEvent;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        return createErrorResponse(AppEvent.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMyBatisException(MyBatisSystemException ex) {
        return createErrorResponse(AppEvent.DATABASE_MYBATIS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleOtherException(Exception ex) {
        return createErrorResponse(AppEvent.UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(AppEvent event, HttpStatus status, Exception ex) {
        log.error("Error={}", event, ex);
        return ResponseEntity.status(status).body(ErrorResponse.of(event));
    }
}
