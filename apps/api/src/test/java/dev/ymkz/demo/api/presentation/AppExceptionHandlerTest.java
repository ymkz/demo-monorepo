package dev.ymkz.demo.api.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import dev.ymkz.demo.api.presentation.dto.ErrorResponse;
import dev.ymkz.demo.core.domain.event.AppEvent;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("null")
class AppExceptionHandlerTest {

    private final AppExceptionHandler handler = new AppExceptionHandler();

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        var ex = new ValidationException("バリデーションエラー");
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse body = response.getBody();
        assertThat(body.code()).isEqualTo(AppEvent.VALIDATION_FAILED.code());
        assertThat(body.message()).isEqualTo(AppEvent.VALIDATION_FAILED.message());
    }

    @Test
    void handleMyBatisException_ShouldReturnInternalServerError() {
        var ex = new MyBatisSystemException("MyBatisエラー", new RuntimeException(""));
        ResponseEntity<ErrorResponse> response = handler.handleMyBatisException(ex);

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse body = response.getBody();
        assertThat(body.code()).isEqualTo(AppEvent.DATABASE_MYBATIS_ERROR.code());
        assertThat(body.message()).isEqualTo(AppEvent.DATABASE_MYBATIS_ERROR.message());
    }

    @Test
    void handleOtherException_ShouldReturnInternalServerError() {
        var ex = new RuntimeException("予期せぬエラー");
        ResponseEntity<ErrorResponse> response = handler.handleOtherException(ex);

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse body = response.getBody();
        assertThat(body.code()).isEqualTo(AppEvent.UNEXPECTED_ERROR.code());
        assertThat(body.message()).isEqualTo(AppEvent.UNEXPECTED_ERROR.message());
    }
}
