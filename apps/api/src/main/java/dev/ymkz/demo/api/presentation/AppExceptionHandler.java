package dev.ymkz.demo.api.presentation;

import dev.ymkz.demo.api.presentation.dto.ErrorResponse;
import dev.ymkz.demo.core.domain.event.AppEvent;
import jakarta.validation.ValidationException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(AppEvent.VALIDATION_FAILED));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMyBatisException(MyBatisSystemException ex) {
        return ResponseEntity.internalServerError().body(ErrorResponse.of(AppEvent.DATABASE_MYBATIS_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleOtherException(Exception ex) {
        return ResponseEntity.internalServerError().body(ErrorResponse.of(AppEvent.DATABASE_ACCESS_FAILED));
    }
}
