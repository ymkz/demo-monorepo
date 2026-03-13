---
name: wide-event-logging
description: Spring Bootアプリケーションで1リクエストあたりの全イベントを1つのJSONログに集約する。MDCを使用し、リクエスト処理のトレーサビリティ向上とパフォーマンス分析を実現する。1リクエストの処理が複数ログ行に分散している、ボトルネック分析が必要、分散トレーシング基盤が欲しい場合に使用する。
license: Proprietary
compatibility: Spring Boot 3.x, Java 17+, logstash-logback-encoder
metadata:
  author: ymkz
  version: "1.0"
  language: java
---

# Wide Event Logging

1リクエストの全イベント（DBクエリ、業務処理、エラー等）を1つのJSONログに集約するパターン。

## Quick Start

```java
// Controller層での使用例
@GetMapping("/books")
public Response search(Query query) {
    var start = System.currentTimeMillis();
    var result = usecase.execute(query);
    
    EventsCollector.record(
        "business_logic",      // event type
        "book_search",         // event name
        System.currentTimeMillis() - start,  // duration
        Map.of("total", result.size())       // metadata
    );
    
    return Response.of(result);
}
```

## Implementation Steps

### Step 1: Data Structure (WideEventLog.java)

```java
package com.example.infrastructure.logging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WideEventLog {
    private String requestId;
    private String method;
    private String path;
    private Instant startTime;
    private Long durationMs;
    private Integer statusCode;
    private String userAgent;

    @Builder.Default
    private List<Event> events = new ArrayList<>();
    private ErrorInfo error;

    @Data
    @Builder
    public static class Event {
        private Instant timestamp;
        private String type;
        private String name;
        private Long durationMs;
        private Object metadata;
    }

    @Data
    @Builder
    public static class ErrorInfo {
        private String type;
        private String name;
        private Instant occurredAt;
        private String errorType;
        private String errorMessage;
        private Object metadata;
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }
}
```

### Step 2: ThreadLocal Manager (EventsCollector.java)

```java
package com.example.infrastructure.logging;

import java.time.Instant;
import java.util.UUID;

public class EventsCollector {
    private static final int MAX_EVENTS = 100;
    private static final ThreadLocal<WideEventLog> holder = new ThreadLocal<>();

    public static void initialize(String method, String path, String userAgent) {
        WideEventLog log = WideEventLog.builder()
                .requestId(UUID.randomUUID().toString())
                .method(method)
                .path(path)
                .startTime(Instant.now())
                .userAgent(userAgent)
                .build();
        holder.set(log);
    }

    public static void record(String type, String name, Long durationMs, Object metadata) {
        WideEventLog log = holder.get();
        if (log == null) return;

        if (log.getEvents().size() >= MAX_EVENTS) {
            log.getEvents().remove(0);
        }

        log.addEvent(WideEventLog.Event.builder()
                .timestamp(Instant.now())
                .type(type)
                .name(name)
                .durationMs(durationMs)
                .metadata(metadata)
                .build());
    }

    public static void setError(String type, String name, Exception ex, Object metadata) {
        WideEventLog log = holder.get();
        if (log == null) return;

        log.setError(WideEventLog.ErrorInfo.builder()
                .type(type)
                .name(name)
                .occurredAt(Instant.now())
                .errorType(ex.getClass().getSimpleName())
                .errorMessage(ex.getMessage())
                .metadata(metadata)
                .build());
    }

    public static WideEventLog finalizeLog(int statusCode) {
        WideEventLog log = holder.get();
        if (log != null) {
            log.setDurationMs(Instant.now().toEpochMilli() - log.getStartTime().toEpochMilli());
            log.setStatusCode(statusCode);
        }
        return log;
    }

    public static String getRequestId() {
        WideEventLog log = holder.get();
        return log != null ? log.getRequestId() : null;
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
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class WideEventLoggingFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        EventsCollector.initialize(
                httpReq.getMethod(),
                httpReq.getRequestURI(),
                httpReq.getHeader("User-Agent"));

        try {
            chain.doFilter(request, response);
        } finally {
            WideEventLog finalLog = EventsCollector.finalizeLog(httpRes.getStatus());
            if (finalLog != null) {
                try {
                    String json = objectMapper.writeValueAsString(finalLog);
                    log.info("WIDE_EVENT {}", json);
                } catch (Exception e) {
                    log.error("Failed to serialize WideEventLog", e);
                }
            }
            EventsCollector.clear();
        }
    }
}
```

## Usage Patterns

### Pattern 1: Controller Layer

```java
var start = System.currentTimeMillis();
var data = bookSearchUsecase.execute(query);

EventsCollector.record(
    "business_logic",
    "book_search",
    System.currentTimeMillis() - start,
    new SearchMetadata(query, offset, limit, data.totalCount())
);

private record SearchMetadata(Object query, int offset, int limit, long totalResults) {}
```

### Pattern 2: Exception Handler

```java
@ExceptionHandler
public ResponseEntity handle(Exception ex) {
    String requestId = EventsCollector.getRequestId();
    log.error("Error requestId={}", requestId, ex);
    
    EventsCollector.setError(
        "validation",
        "request_validation",
        ex,
        null
    );
    
    return ResponseEntity.status(400).body(ErrorResponse.of(ex));
}
```

### Pattern 3: Usecase Layer with DB Query

```java
var dbStart = System.currentTimeMillis();
var books = repository.download(query);
EventsCollector.record(
    "db_query",
    "book_download",
    System.currentTimeMillis() - dbStart,
    Map.of("rowCount", books.size())
);
```

### Pattern 4: Error Handling

```java
var start = System.currentTimeMillis();
try {
    var result = convert(data);
    EventsCollector.record("data_conversion", "csv_generation", 
        System.currentTimeMillis() - start, Map.of("rows", data.size()));
    return result;
} catch (JacksonException ex) {
    log.error("Failed requestId={}", EventsCollector.getRequestId(), ex);
    EventsCollector.setError("data_conversion", "csv_generation", ex, 
        Map.of("rows", data.size()));
    throw new RuntimeException("Conversion failed", ex);
}
```

## Output Format

### Success Response

```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "method": "GET",
  "path": "/books",
  "startTime": "2026-03-13T10:00:00.000Z",
  "durationMs": 23,
  "statusCode": 200,
  "events": [
    {
      "timestamp": "2026-03-13T10:00:00.005Z",
      "type": "db_query",
      "name": "BookMapper.search",
      "durationMs": 5,
      "metadata": {"commandType": "SELECT"}
    },
    {
      "timestamp": "2026-03-13T10:00:00.012Z",
      "type": "business_logic",
      "name": "book_search",
      "durationMs": 15,
      "metadata": {"totalResults": 42}
    }
  ]
}
```

### Error Response

```json
{
  "statusCode": 500,
  "durationMs": 150,
  "events": [],
  "error": {
    "type": "data_conversion",
    "name": "csv_generation",
    "occurredAt": "2026-03-13T10:00:00.150Z",
    "errorType": "JacksonException",
    "errorMessage": "Invalid UTF-8 character",
    "metadata": {"rowCount": 10000}
  }
}
```

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| `events` array for successes only | Separation of concerns; simplifies success/failure queries |
| `error` field at top level | Easy filtering with `ispresent(error)` in log queries |
| Stack traces excluded from Wide Event | Keep JSON size manageable; output separately with `log.error()` |
| 100 event limit with FIFO eviction | Memory protection against runaway logging |
| ThreadLocal with mandatory `clear()` | Request isolation; prevents memory leaks |
| ISO-8601 timestamps with `Instant` | Standard format; timezone-aware |

## Query Examples

### CloudWatch Logs Insights

```sql
-- Find error requests
fields @timestamp, requestId, path, statusCode
| filter ispresent(error)
| sort @timestamp desc

-- Specific error type
fields @timestamp, requestId, error.errorType
| filter error.type = 'db_query'

-- Average duration by endpoint
fields durationMs, path
| stats avg(durationMs) by path
| sort avg(durationMs) desc

-- Requests with many events (potential complexity issues)
fields @timestamp, requestId, events
| stats count(events) as eventCount by requestId
| sort eventCount desc
| limit 100
```

## Warnings

- **Do not include PII in metadata** - Email addresses, phone numbers, etc. should be masked or omitted
- **Watch JSON size** - Large metadata objects can inflate log volume. Consider sampling in production
- **Handle request interruption** - JVM shutdown may lose in-flight events. Critical events should also use immediate `log.info/warn`
- **Avoid circular references** - Jackson will fail to serialize entities with bidirectional relationships. Use DTOs or Maps for metadata

## Edge Cases

| Scenario | Behavior |
|----------|----------|
| EventsCollector called before initialization | Silently ignored (null check) |
| More than 100 events | Oldest events removed (FIFO) |
| Exception during JSON serialization | Logged separately; request continues |
| Nested error calls | Last call wins (overwrites previous error) |
| Async processing | ThreadLocal isolation breaks - use explicit context propagation |

## References

- ADR: [docs/adr/20260313-wide-event-logging.md](../../../docs/adr/20260313-wide-event-logging.md)
- Implementation: [apps/api/src/main/java/dev/ymkz/demo/api/infrastructure/logging/](../../../apps/api/src/main/java/dev/ymkz/demo/api/infrastructure/logging/)
