# ADR 002: Request ScopeベースWide Event Loggingへの移行

## ステータス

**Accepted** - 2026-06-25

## コンテキスト

ADR 001では、`EventsCollector` と `ThreadLocal` を使って1リクエストのイベントを集約するWide Event Loggingを導入した。
この方式は同期HTTPリクエストでは動作するが、以下の課題があった。

1. `EventsCollector` がstatic APIであり、利用側の依存関係が明示されない
2. テスト時に差し替えにくい
3. Spring MVCの通常の同期HTTPリクエストでは、Springのrequest scopeでリクエスト単位の状態を表現できる
4. evlog.devに近いwide eventとしては、`events[]` だけでなくトップレベルのkey-valueも扱いたい

## 決定

Wide Event Loggingのコンテキスト管理を、ThreadLocalベースの`EventsCollector`からrequest scopeの`EventLogContext`へ移行する。

### 採用する設計

- `EventLogContext` はrequest scopeにする
- `proxyMode = ScopedProxyMode.TARGET_CLASS` を指定し、Controller / UseCase / ExceptionHandlerなどのsingleton beanへ通常のコンストラクタインジェクションで渡せるようにする
- `EventLogContext#set(key, value)` でwide eventのトップレベルフィールドを追加する
- `EventLogContext#addEvent(msg, metadata)` で従来の時系列イベントも保持する
- `EventLogContext#setError(Throwable, metadata)` で例外情報を保持する
- `WideEventLoggingFilter` は `ObjectProvider<EventLogContext>` 経由で現在リクエストのコンテキストを取得する
- `requestId` のMDC設定は継続する
- `EventsCollector` は削除する

### データフロー

```text
HTTP Request
  ↓
WideEventLoggingFilter
  - requestIdを生成してMDCの`http.request.id`へ設定
  ↓
Controller / UseCase / ExceptionHandler
  - EventLogContext#set(...) で検索しやすいwide fieldを追加
  - EventLogContext#addEvent(...) で時系列イベントを追加
  - EventLogContext#setError(...) で例外情報を追加
  ↓
WideEventLoggingFilter finally
  - EventLogContext#snapshot() を取得
  - `http.request.method` / `url.path` / `http.response.status_code` / `event.duration` / fields / events / error を1件のJSON structured logとして出力
  - MDCをクリア
```

## ログ構造

意味的には以下のような1イベントを出力する。

```json
{
  "@timestamp": "2026-06-25T00:00:00.000+09:00",
  "severity": "INFO",
  "msg": "response_success",
  "http.request.id": "550e8400-e29b-41d4-a716-446655440000",
  "http.request.method": "GET",
  "url.path": "/books",
  "event.start": "2026-06-25T00:00:00.000+09:00",
  "event.end": "2026-06-25T00:00:00.123+09:00",
  "event.duration": 123000000,
  "http.response.status_code": 200,
  "book.search.total_results": 42,
  "events": [
    {
      "timestamp": "2026-06-25T00:00:00.100+09:00",
      "msg": "book_search_executed",
      "metadata": {
        "totalResults": 42
      }
    }
  ],
  "error": null
}
```

## 利用方針

`EventLogContext` を使ってよい場所は以下とする。

- Controller
- Application Service / UseCase
- Infrastructure Adapter
- ExceptionHandler

使わない場所は以下とする。

- Domain Model
- 純粋なDomain Service
- Entity
- Value Object

`EventLogContext` はSpring / logging / infrastructure寄りの関心であり、ドメイン層へ混ぜない。

## 非同期処理の扱い

初期スコープはSpring MVCの通常の同期HTTPリクエストに限定する。

以下は別途設計する。

- `@Async`
- `CompletableFuture`
- 独自スレッドプール
- Reactor
- Pulsar consumer
- batch
- scheduled job

request scopeは通常、現在のHTTPリクエストに紐づくため、別スレッドへそのまま伝播する設計にはしない。

## 影響

### ポジティブ

1. static APIとThreadLocalを削除できる
2. 依存関係がコンストラクタインジェクションで明示される
3. request scopeによりリクエスト間の混線をSpringに任せられる
4. トップレベルkey-valueによりログ検索・集計がしやすくなる
5. 従来の`events[]`も残すため、時系列イベントも追跡できる

### ネガティブ / リスク

| リスク | 対策 |
|--------|------|
| HTTPリクエスト外で`EventLogContext`を使うとrequest scopeが存在しない | HTTP外処理は別設計にする |
| 同一リクエスト内で複数スレッドから書き込むと`LinkedHashMap`/`ArrayList`がスレッドセーフでない | 初期スコープは同期HTTPリクエストに限定する |
| ログフィールド名が無秩序に増える | 命名規約をSkillに記載し、レビューで統制する |

## 実装サマリー

- 追加: `apps/api/src/main/java/dev/ymkz/demo/api/shared/logging/EventLogContext.java`
- 変更: `apps/api/src/main/java/dev/ymkz/demo/api/shared/logging/WideEventLoggingFilter.java`
- 削除: `apps/api/src/main/java/dev/ymkz/demo/api/shared/logging/EventsCollector.java`
- 変更: `BookController`, `BookDownloadUsecase`, `AppExceptionHandler`
- 変更: `WideEventLoggingFilterTest`, `BookDownloadUsecaseTest`

## 関連ドキュメント

- ADR 001: `docs/adr/20260313-wide-event-logging.md`
- Agent Skill: `.agents/skills/wide-event-logging/SKILL.md`
