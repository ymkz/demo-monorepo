---
name: wide-event-logging
description: Spring Bootアプリケーションで1リクエストあたりの全イベントを1つのJSONログに集約する。MDCを使用し、リクエスト処理のトレーサビリティ向上とパフォーマンス分析を実現する。1リクエストの処理が複数ログ行に分散している、ボトルネック分析が必要、分散トレーシング基盤が欲しい場合に使用する。
license: Proprietary
compatibility: Spring Boot 3.x, Java 17+, logstash-logback-encoder
metadata:
  author: ymkz
  version: "1.1"
  language: java
---

# Wide Event Logging

1リクエストの全イベント（DBクエリ、業務処理、エラー等）を1つのJSONログに集約するパターン。

## Quick Start

```java
// Controller層での使用例
@GetMapping("/books")
public Response search(Query query) {
    var result = usecase.execute(query);
    
    EventsCollector.record(
        "book_search_executed",  // メッセージ
        Map.of("total", result.size())  // metadata
    );
    
    return Response.of(result);
}
```

## Implementation Steps

### Step 1: Data Structure (WideEventLog.java)

```java
package com.example.infrastructure.logging;

import java.time.ZonedDateTime;
import java.util.List;

public record WideEventLog(
        String requestId,
        String method,
        String path,
        ZonedDateTime requestedAt,
        ZonedDateTime respondedAt,
        Long durationMs,
        Integer statusCode,
        List<Event> events,
        ErrorInfo error) {

    public record Event(ZonedDateTime timestamp, String msg, Object metadata) {}

    public record ErrorInfo(ZonedDateTime occurredAt, String exception, String msg, Object metadata) {}
}
```

### Step 2: ThreadLocal Manager (EventsCollector.java)

```java
package com.example.infrastructure.logging;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventsCollector {
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
    private static final ThreadLocal<Context> holder = new ThreadLocal<>();

    private record Context(
            String requestId,
            String method,
            String path,
            ZonedDateTime requestedAt,
            List<WideEventLog.Event> events,
            WideEventLog.ErrorInfo error) {}

    public static String initialize(String method, String path) {
        String requestId = UUID.randomUUID().toString();
        Context ctx = new Context(
                requestId, method, path, ZonedDateTime.now(JST), 
                new ArrayList<>(), null);
        holder.set(ctx);
        return requestId;
    }

    public static void record(String msg, Object metadata) {
        Context ctx = holder.get();
        if (ctx == null) return;

        ctx.events.add(new WideEventLog.Event(
                ZonedDateTime.now(JST), msg, metadata));
    }

    public static void setError(Exception ex, Object metadata) {
        Context ctx = holder.get();
        if (ctx == null) return;

        WideEventLog.ErrorInfo errorInfo = new WideEventLog.ErrorInfo(
                ZonedDateTime.now(JST),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                metadata);

        holder.set(new Context(
                ctx.requestId, ctx.method, ctx.path, ctx.requestedAt,
                ctx.events, errorInfo));
    }

    public static WideEventLog finalizeLog(int statusCode) {
        Context ctx = holder.get();
        if (ctx == null) return null;

        ZonedDateTime now = ZonedDateTime.now(JST);
        long durationMs = now.toInstant().toEpochMilli()
                - ctx.requestedAt.toInstant().toEpochMilli();

        return new WideEventLog(
                ctx.requestId, ctx.method, ctx.path,
                ctx.requestedAt, now, durationMs, statusCode,
                ctx.events, ctx.error);
    }

    public static String getRequestId() {
        Context ctx = holder.get();
        return ctx != null ? ctx.requestId : null;
    }

    public static void clear() {
        holder.remove();
    }
}
```

### Step 3: Servlet Filter (WideEventLoggingFilter.java)

```java
package com.example.infrastructure.logging;

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

        String requestId = EventsCollector.initialize(
                httpReq.getMethod(), httpReq.getRequestURI());
        MDC.put("requestId", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            WideEventLog finalLog = EventsCollector.finalizeLog(httpRes.getStatus());
            if (finalLog != null) {
                try {
                    Map<String, Object> fields = objectMapper.convertValue(finalLog, Map.class);
                    fields.remove("requestId");
                    StructuredArgument[] args = fields.entrySet().stream()
                            .map(e -> StructuredArguments.value(e.getKey(), e.getValue()))
                            .toArray(StructuredArgument[]::new);

                    if (finalLog.error() != null) {
                        log.error("WIDE_EVENT", args);
                    } else {
                        log.info("WIDE_EVENT", args);
                    }
                } catch (Exception e) {
                    log.error("Failed to serialize WideEventLog", e);
                }
            }
            EventsCollector.clear();
            MDC.remove("requestId");
        }
    }
}
```

## Usage Patterns

### Pattern 1: Controller Layer

```java
@GetMapping("/books")
public SearchBooksResponse searchBooks(SearchQuery query) {
    var data = bookSearchUsecase.execute(query);
    
    EventsCollector.record(
        "book_search_executed",
        new SearchMetadata(query, data.totalCount()));
    
    return SearchBooksResponse.of(data);
}

private record SearchMetadata(Object query, long totalResults) {}
```

### Pattern 2: Exception Handler

```java
@ExceptionHandler(HandlerMethodValidationException.class)
public ResponseEntity<Object> handleValidationException(HandlerMethodValidationException ex) {
    EventsCollector.setError(ex, null);
    return super.handleHandlerMethodValidationException(ex, headers, status, request);
}
```

### Pattern 3: Usecase Layer

```java
public List<Book> findBooks(Query query) {
    var books = bookMapper.select(query);
    EventsCollector.record("book_select_executed", Map.of("rowCount", books.size()));
    return books;
}
```

### Pattern 4: Error Handling

```java
try {
    var result = convert(data);
    EventsCollector.record("csv_generation_executed", new CsvMetadata(data.size()));
    return result;
} catch (JacksonException ex) {
    EventsCollector.setError(ex, new CsvMetadata(data.size()));
    throw new RuntimeException("Conversion failed", ex);
}
```

## Output Format

### Success Response

```json
{
  "loggedAt": "2026-03-14T01:21:09.744+09:00",
  "severity": "INFO",
  "msg": "WIDE_EVENT",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "method": "GET",
  "path": "/books",
  "requestedAt": "2026-03-14T01:21:09.422+09:00",
  "respondedAt": "2026-03-14T01:21:09.717+09:00",
  "durationMs": 295,
  "statusCode": 200,
  "events": [
    {
      "timestamp": "2026-03-14T01:21:09.679+09:00",
      "msg": "book_search_executed",
      "metadata": {"totalResults": 42}
    }
  ],
  "error": null
}
```

### Error Response

```json
{
  "loggedAt": "2026-03-14T01:17:49.475+09:00",
  "severity": "ERROR",
  "msg": "WIDE_EVENT",
  "requestId": "...",
  "method": "GET",
  "path": "/books",
  "requestedAt": "...",
  "respondedAt": "...",
  "durationMs": 103,
  "statusCode": 400,
  "events": [],
  "error": {
    "occurredAt": "2026-03-14T01:17:49.418+09:00",
    "exception": "HandlerMethodValidationException",
    "msg": "400 BAD_REQUEST \"Validation failure\"",
    "metadata": null
  }
}
```

## Design Decisions

| 項目 | 決定事項 |
|------|----------|
| **データ構造** | Java Recordで不変性を保証 |
| **イベント** | `msg`フィールドのみ（`type`/`name`は廃止） |
| **エラー情報** | `exception`/`msg`のみ（`type`/`name`は廃止） |
| **severity** | error有無で自動切り替え（INFO/ERROR） |
| **時刻** | JST（Asia/Tokyo）で統一、ISO-8601形式 |
| **イベント制限** | 現状は無制限（必要に応じて将来検討） |
| **ThreadLocal** | リクエストスレッドごとに分離。finallyで必ずclear |

## Query Examples

### CloudWatch Logs Insights

```sql
-- エラーリクエスト抽出
fields @timestamp, requestId, path, statusCode
| filter severity = "ERROR"
| sort @timestamp desc

-- 特定例外タイプ検索
fields @timestamp, requestId, error.exception
| filter error.exception = "HandlerMethodValidationException"

-- 平均処理時間の集計
fields durationMs, path
| stats avg(durationMs) by path
| sort avg(durationMs) desc

-- 特定イベントを含むリクエスト
fields @timestamp, requestId, events
| filter events[*].msg = "book_search_executed"
```

## Warnings

- **PII取り扱い**: `metadata`に個人情報を含めない。必要ならマスキング処理追加
- **ログサイズ**: 大量イベント時はサイズ増大。開発環境でのみ有効化も検討
- **リクエスト中断**: JVMシャットダウン時の情報消失を防ぐため、重要イベントは即座出力も併用
- **循環参照**: `metadata`にエンティティなど循環参照を持つオブジェクトを渡さない

## Edge Cases

| シナリオ | 動作 |
|----------|------|
| EventsCollector呼び出し前に初期化なし | 無視（nullチェック） |
| 例外時のJSONシリアライズ失敗 | エラーログ出力、リクエストは継続 |
| Async処理（@Async等） | ThreadLocalが分離されるため未対応 |

## References

- ADR: `docs/adr/20260313-wide-event-logging.md`
- Implementation: `apps/api/src/main/java/dev/ymkz/demo/api/infrastructure/logging/`
