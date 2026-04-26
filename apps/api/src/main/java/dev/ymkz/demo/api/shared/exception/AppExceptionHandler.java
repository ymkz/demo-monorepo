package dev.ymkz.demo.api.shared.exception;

import dev.ymkz.demo.api.shared.logging.EventsCollector;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
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
        EventsCollector.setError(ex, null);
        return super.handleHandlerMethodValidationException(ex, headers, status, request);
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
        EventsCollector.setError(ex, null);
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
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
        EventsCollector.setError(ex, null);
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
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
        EventsCollector.setError(ex, null);
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
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
        EventsCollector.setError(ex, null);
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
