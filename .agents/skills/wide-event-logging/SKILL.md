---
name: wide-event-logging
description: このプロジェクトでは1リクエストあたりの文脈情報とイベントを1つのJSONログに集約する「ワイドイベントロギング」を採用している。request scopeのEventLogContextとMDCを組み合わせ、リクエスト処理のトレーサビリティ向上とパフォーマンス分析を実現する。
license: Proprietary
compatibility: Spring Boot 4.x, Java 21+, logstash-logback-encoder
metadata:
  author: ymkz
  version: "3.0"
  language: java
---

# Wide Event Logging 方針

## 核心理念

1リクエストの文脈情報、時系列イベント、エラー情報を**1つのJSONログ**に集約する。

目的は以下である。

- **トレーサビリティ**: requestIdでリクエスト単位の処理を追跡する
- **検索・集計しやすさ**: トップレベルkey-valueでログ基盤から集計しやすくする
- **時系列把握**: `events[]`でリクエスト内の重要イベントを順序付きで残す
- **構造化ログ**: JSON形式でログ基盤と連携する

## 現行アーキテクチャ

```text
HTTP Request
  ↓
WideEventLoggingFilter
  - requestIdを生成してMDCの`http.request.id`へ設定
  ↓
Controller / UseCase / ExceptionHandler
  - EventLogContext#set(...) でwide fieldを追加
  - EventLogContext#addEvent(msg, key, value, ...) で時系列イベントを追加
  - EventLogContext#setError(...) で例外情報を追加
  ↓
WideEventLoggingFilter finally
  - EventLogContext#snapshot()を取得
  - 1件のJSON structured logとして出力
  - MDCをクリア
```

## 実装方針

### EventLogContext

`EventLogContext` はrequest scopeにする。

- `@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)` を使う
- Controller / UseCase / ExceptionHandlerには通常のコンストラクタインジェクションで渡す
- Filterでは `ObjectProvider<EventLogContext>` 経由で取得する
- `snapshot()` は内部の `Map` / `List` を直接返さず、コピーした変更不可コレクションを返す

### ThreadLocalは禁止

旧実装の `EventsCollector` / `ThreadLocal` は廃止済み。

新規実装で以下を使ってはいけない。

```java
EventsCollector.addEvent(...); // NG
new ThreadLocal<>();           // NG: wide event context用途では使わない
```

### ハイブリッド記録

wide eventでは2種類の情報を使い分ける。

#### 1. トップレベルフィールド

検索・集計したい値は `set(...)` でトップレベルに出す。

```java
eventLog.set("book.search.total_results", data.totalCount());
eventLog.set("book.download.row_count", books.size());
```

#### 2. 時系列イベント

処理の順序やイベント発生を残したい場合は `addEvent(...)` を使う。
metadataはkey-valueの可変長引数で渡す。

```java
eventLog.addEvent(
        "book_search_executed",
        "isbn",
        queryParam.isbn(),
        "title",
        queryParam.title(),
        "offset",
        offset,
        "limit",
        limit,
        "total_results",
        data.totalCount());
```

## 使ってよい場所

```text
Controller
Application Service / UseCase
Infrastructure Adapter
ExceptionHandler
```

## 使わない場所

```text
Domain Model
純粋なDomain Service
Entity
Value Object
```

`EventLogContext` はSpring / logging / infrastructure寄りの関心である。
ドメイン層へ混ぜてはいけない。

## 命名規約

### トップレベルフィールド

- `.` 区切りのlower snake/camel混在を避け、既存に合わせて読みやすく付ける
- 集計したい値は意味がわかる名前にする

例:

```text
book.search.total_results
book.download.row_count
csv.generation.row_count
```

### イベント名

イベント名は `snake_case` で動詞終わりにする。

良い例:

```text
book_search_executed
csv_generation_executed
payment_processing_started
```

悪い例:

```text
bookSearch
SEARCH_BOOK
book_search
```

## メタデータ設計

### 良い例

必要最小限の値だけを渡す。

```java
eventLog.addEvent("book_search_executed", "isbn", isbn, "title", title, "total_results", totalResults);
```

### 避ける例

```java
eventLog.addEvent("book_search_executed", "book", bookEntity); // NG: 大きなオブジェクトをmetadataへ入れない
```

避ける理由:

- 循環参照でシリアライズに失敗する可能性がある
- ログサイズが肥大化する
- PIIや不要な内部情報が混入しやすい

## エラー記録

ExceptionHandlerでは `setError(...)` を呼ぶ。

```java
eventLog.setError(ex, null);
```

個別処理で補足して再スローする場合も、必要に応じて記録してから再スローする。

```java
try {
    process();
} catch (CsvException ex) {
    eventLog.setError(ex, Map.of("row_count", rowCount));
    throw ex;
}
```

例外を握りつぶしてはいけない。

## 非同期処理の扱い

初期スコープは通常のSpring MVC同期HTTPリクエストに限定する。

以下は別途設計する。

- `@Async`
- `CompletableFuture`
- 独自スレッドプール
- Reactor
- Pulsar consumer
- batch
- scheduled job

request scopeは別スレッドへ自動伝播する前提にしない。

## テスト方針

必須で確認すること。

1. 正常終了時にMDCの`http.request.id`がクリアされること
2. 例外発生時にもMDCの`http.request.id`がクリアされること
3. 未捕捉例外がwide eventの`error`に記録されること
4. `snapshot()` が内部Map/Listを直接公開しないこと
5. UseCase等で `set(...)` / `addEvent(...)` が呼べること

## ログ基盤連携例

### Loki / LogQL

```logql
# エラーリクエスト抽出
{app="demo-api"} | json | severity="ERROR"

# 特定イベントを含むリクエスト
{app="demo-api"} | json | line_format "{{.events}}" |~ "book_search_executed"

# 処理時間が閾値を超えたリクエスト（duration_msはミリ秒）
{app="demo-api"} | json | duration_ms > 1000
```

## 参考資料

- ADR 001: `docs/adr/20260313-wide-event-logging.md`
- ADR 002: `docs/adr/20260625-request-scope-wide-event-logging.md`
- 実装: `apps/api/src/main/java/dev/ymkz/demo/api/shared/logging/`
- テスト: `apps/api/src/test/java/dev/ymkz/demo/api/shared/logging/WideEventLoggingFilterTest.java`
