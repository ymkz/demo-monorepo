# ADR 001: MDCベースWide Event Loggingの導入

## ステータス

**Accepted** - 2026-03-13

## 実装サマリー

- `WideEventLog.java` - Java Recordで定義されたデータ構造（`events`配列 + `error`フィールド）
- `EventsCollector.java` - ThreadLocalベースのイベント収集
- `WideEventLoggingFilter.java` - Servlet Filter（Order=1で最初に実行）
- 既存ログ移行完了: `BookController`, `AppExceptionHandler`, `BookDownloadUsecase`

## コンテキスト

現在のAPI（`apps/api`）では以下の箇所でログ出力を行っている：

- `BookController.searchBooks()` - INFOログ（クエリパラメータ記録）
- `AppExceptionHandler` - ERRORログ（例外発生時）
- `BookDownloadUsecase` - ERRORログ（CSV変換失敗時）

### 現状の課題

1. **ログの分散**: リクエスト処理が複数のログ行に分散し、トレーサビリティが低下
2. **コンテキストの欠如**: リクエストIDで紐付けが必要だが、検索が困難
3. **パフォーマンス分析の困難さ**: 処理時間の計測が個別実装で統一性がない

### 検討した代替案

| 代替案 | 評価 |
|--------|------|
| **A. 既存ログ維持** | 実装コストゼロだが、モノレポ拡張時に技術的負債が蓄積 |
| **B. OpenTelemetry** | 最も完全だが、学習コストとインフラ要件が高い |
| **C. MDC + Wide Event Logging**（採用） | Spring Boot標準機能のみで実現可能。段階的導入が可能 |

## 決定

**MDC（Mapped Diagnostic Context）を使用したWide Event Loggingを導入する。**

### 設計方針

```
┌─────────────────────────────────────────────────────────────┐
│  リクエスト開始                                              │
│    ↓                                                        │
│  [WideEventLoggingFilter] MDC初期化                          │
│    - requestId生成                                           │
│    - EventsCollectorに保持                                   │
│    ↓                                                        │
│  各レイヤーで EventsCollector.record() を呼び出し            │
│    ↓                                                        │
│  [WideEventLoggingFilter] レスポンス返却前に出力             │
│    - WideEventLogレコードを生成                              │
│    - 1行のJSONとしてWide Eventを出力                         │
│    - severityはerror有無で自動切り替え（INFO/ERROR）         │
│    - MDCクリア                                               │
└─────────────────────────────────────────────────────────────┘
```

### データ構造

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
      "metadata": { "totalResults": 42 }
    }
  ],
  "error": null
}
```

#### エラー時の構造

```json
{
  "severity": "ERROR",
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

### 時刻フィールドの意味

| フィールド | 意味 | タイムゾーン |
|-----------|------|-------------|
| `loggedAt` | ログが記録された時刻 | JST (+09:00) |
| `requestedAt` | リクエスト開始時刻 | JST (+09:00) |
| `respondedAt` | レスポンス返却時刻 | JST (+09:00) |
| `events[].timestamp` | 各イベント発生時刻 | JST (+09:00) |
| `error.occurredAt` | エラー発生時刻 | JST (+09:00) |

### severityの自動切り替え

- `error` フィールドが `null` の場合: `severity: "INFO"`
- `error` フィールドが存在する場合: `severity: "ERROR"`

## 影響

### ポジティブ

1. **トレーサビリティの向上**: 1リクエストの全ての処理を1JSONで追跡可能
2. **パフォーマンス分析**: ボトルネック特定が容易（`requestedAt`/`respondedAt`/`durationMs`）
3. **クエリの簡易化**: `severity`/`error`フィールドの有無で成功/失敗を即座に判定
4. **モノレポ親和性**: `web-form`, `web-tool` との分散トレーシング基盤に再利用可能
5. **段階的導入**: 既存ログを置き換えず、並行運用が可能

### ネガティブ/リスク

| リスク | 対策 |
|--------|------|
| メモリ増大（大量イベント時） | 現状は無制限。必要に応じて将来制限を検討 |
| ログサイズ増加 | 開発環境でのみ有効化、またはサンプリングを検討 |
| リクエスト中断時の情報消失 | JVMシャットダウンフックでflush、重要イベントは即座に出力 |
| 機密情報の漏洩 | metadataにはPIIを含めない。必要な場合はマスキング処理を追加 |

## 実装計画

### Phase 1: 基盤実装（完了）

- [x] `WideEventLog` - Java Recordでデータ構造定義
- [x] `EventsCollector` - ThreadLocal管理とイベント収集
- [x] `WideEventLoggingFilter` - Servlet Filter実装
- [x] `logback-spring.xml` - JSONエンコーダー設定

### Phase 2: 既存ログ移行（完了）

- [x] `BookController.searchBooks()` → `EventsCollector.record()`
- [x] `AppExceptionHandler` → `EventsCollector.setError()`
- [x] `BookDownloadUsecase` → `EventsCollector.record()`

### Phase 3: 検証（完了）

- [x] ローカル環境でログ出力確認
- [x] CloudWatch Logs Insightsクエリの検証
- [x] パフォーマンス影響測定（スループット比較）

### Phase 4: 拡張（将来）

- [ ] MyBatis InterceptorによるSQL実行の自動記録
- [ ] `@Timed`アノテーションによる宣言的計測
- [ ] 分散トレーシング（`web-form`/`web-tool`連携）

## 関連ドキュメント

- [Spring Boot公式: Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [logstash-logback-encoder](https://github.com/logfellow/logstash-logback-encoder)
- Agent Skill: `.agents/skills/wide-event-logging/SKILL.md`

## 備考

- 本ADRは日本語で記述（プロジェクト規約に準拠）
- 設計変更が発生した場合は、新規ADRを作成し本ADRを「Superseded」に変更
