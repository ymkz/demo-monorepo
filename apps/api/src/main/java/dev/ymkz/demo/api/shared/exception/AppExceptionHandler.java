package dev.ymkz.demo.api.shared.exception;

import dev.ymkz.demo.api.shared.logging.EventLogContext;
import dev.ymkz.demo.core.domain.event.AppEvent;
import java.net.URI;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    private final EventLogContext eventLog;

    /**
     * コントローラーのハンドラーメソッドに対するバリデーションで発生する例外を処理する。
     *
     * <p>{@code @RequestParam}、{@code @PathVariable}、{@code @RequestBody} などに付与した Bean Validation
     * アノテーションの検証に失敗した場合に発生する。たとえば必須パラメータが空、数値が許容範囲外、文字列長が制約を満たさない場合など。
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validation exception occurred", ex);
        eventLog.setError(ex, null);
        var problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST, AppEvent.VALIDATION_FAILED, request, "リクエストパラメータの検証に失敗しました");
        var errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ValidationError(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        problemDetail.setProperty("errors", errors);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validation exception occurred", ex);
        eventLog.setError(ex, null);
        var problemDetail =
                createProblemDetail(HttpStatus.BAD_REQUEST, AppEvent.VALIDATION_FAILED, request, "リクエストボディの検証に失敗しました");
        problemDetail.setProperty(
                "errors",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(ValidationError::of)
                        .toList());
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    /**
     * MyBatis の実行中に発生したシステム例外を処理する。
     *
     * <p>SQL の構文エラー、マッピング設定の不整合、DB 接続障害など、MyBatis がデータアクセス処理を継続できない場合に発生する。
     * アプリケーション側で復旧できない想定のため、サーバー内部エラーとして扱う。
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<Object> handleMyBatisException(MyBatisSystemException ex, WebRequest request) {
        log.error("Database exception occurred", ex);
        eventLog.setError(ex, null);
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        return handleExceptionInternal(
                ex,
                createProblemDetail(status, AppEvent.DATABASE_MYBATIS_ERROR, request),
                new HttpHeaders(),
                status,
                request);
    }

    /**
     * データベースの整合性制約に違反した場合の例外を処理する。
     *
     * <p>主キー・ユニークキーの重複、外部キー制約違反、NOT NULL 制約違反、カラム長超過など、
     * リクエスト内容が永続化時の制約を満たさない場合に発生する。クライアント入力に起因する可能性が高いため Bad Request として扱う。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation occurred", ex);
        eventLog.setError(ex, null);
        var status = HttpStatus.BAD_REQUEST;
        return handleExceptionInternal(
                ex,
                createProblemDetail(status, AppEvent.DATABASE_ACCESS_FAILED, request),
                new HttpHeaders(),
                status,
                request);
    }

    /**
     * 要求されたリソースが存在しない場合の例外を処理する。
     *
     * <p>ID で検索したエンティティが見つからない、Optional の値が存在しない状態で取得しようとした、
     * コレクションから該当要素を取得できなかった場合などに発生する。HTTP レスポンスでは Not Found として扱う。
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
        log.warn("Resource not found", ex);
        eventLog.setError(ex, null);
        var status = HttpStatus.NOT_FOUND;
        return handleExceptionInternal(
                ex,
                createProblemDetail(status, "リソースが見つかりません", "要求されたリソースは存在しません", request),
                new HttpHeaders(),
                status,
                request);
    }

    /**
     * 上記の個別ハンドラーに該当しない予期しない例外を処理する。
     *
     * <p>実装不備、外部サービス障害、想定外の実行時例外など、原因を特定して個別に扱うべき例外が漏れた場合の最終防衛線。
     * 詳細をログに残し、クライアントにはサーバー内部エラーとして返す。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOtherException(Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred", ex);
        eventLog.setError(ex, null);
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        return handleExceptionInternal(
                ex,
                createProblemDetail(status, AppEvent.UNEXPECTED_ERROR, request),
                new HttpHeaders(),
                status,
                request);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, AppEvent event, WebRequest request) {
        var problemDetail = createProblemDetail(status, status.getReasonPhrase(), event.message(), request);
        problemDetail.setProperty("errorCode", event.code());
        return problemDetail;
    }

    private ProblemDetail createProblemDetail(HttpStatus status, AppEvent event, WebRequest request, String detail) {
        var problemDetail = createProblemDetail(status, status.getReasonPhrase(), detail, request);
        problemDetail.setProperty("errorCode", event.code());
        return problemDetail;
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, WebRequest request) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(title);
        if (request instanceof ServletWebRequest servletWebRequest) {
            problemDetail.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
        }
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        var responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return super.handleExceptionInternal(ex, body, responseHeaders, statusCode, request);
    }

    public record ValidationError(String field, String message) {
        private static ValidationError of(FieldError fieldError) {
            return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}
