# ADR 001: MDCベースWide Event Loggingの導入

## ステータス

**Accepted** - 2026-03-13

## 実装サマリー

- `WideEventLog.java` - データ構造（`events`配列 + `error`フィールド）
- `EventsCollector.java` - ThreadLocal管理（上限100件で古いイベントを破棄）
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
| **C. **MDC + Wide Event Logging**（採用） | Spring Boot標準機能のみで実現可能。段階的導入が可能 |

## 決定

**MDC（Mapped Diagnostic Context）を使用したWide Event Loggingを導入する。**

### 設計方針

```
┌─────────────────────────────────────────────────────────────┐
│  リクエスト開始                                              │
│    ↓                                                        │
│  [WideEventLoggingFilter] MDC初期化                          │
│    - requestId生成                                           │
│    - WideEventLogインスタンスをThreadLocalに保持              │
│    ↓                                                        │
│  各レイヤーで EventsCollector.record() を呼び出し            │
│    ↓                                                        │
│  [WideEventLoggingFilter] レスポンス返却前に出力             │
│    - 1行のJSONとしてWide Eventを出力                         │
│    - MDCクリア                                               │
└─────────────────────────────────────────────────────────────┘
```

### データ構造

```json
{
  "timestamp": "2026-03-13T19:15:26.123Z",
  "level": "INFO",
  "message": "WIDE_EVENT",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "method": "GET",
  "path": "/books",
  "startTime": "2026-03-13T19:15:26.100Z",
  "durationMs": 23,
  "statusCode": 200,
  "userAgent": "Mozilla/5.0...",
  "events": [
    {
      "timestamp": "2026-03-13T19:15:26.105Z",
      "type": "db_query",
      "name": "BookMapper.search",
      "durationMs": 5,
      "metadata": { "commandType": "SELECT" }
    },
    {
      "timestamp": "2026-03-13T19:15:26.112Z",
      "type": "business_logic",
      "name": "book_search",
      "durationMs": 15,
      "metadata": { "totalResults": 42 }
    }
  ],
  "error": null
}
```

#### エラー時の構造

```json
{
  "events": [
    // エラー発生前の成功イベントのみ
  ],
  "error": {
    "type": "db_query",
    "name": "book_search",
    "occurredAt": "2026-03-13T19:15:26.105Z",
    "errorType": "MyBatisSystemException",
    "errorMessage": "Connection refused",
    "metadata": { "timeout": 5000 }
  }
}
```

**重要**: 
- `events`配列には**成功した処理のみ**を記録
- エラー情報はトップレベルの`error`フィールドに分離
- スタックトレースはWide Eventには含めず、従来のERRORログに別途出力

### イベントタイプ定義

| type | 説明 | 使用例 |
|------|------|--------|
| `db_query` | データベース問い合わせ | MyBatis Mapper実行 |
| `business_logic` | 業務ロジック実行 | ユースケース層の処理 |
| `data_conversion` | データ形式変換 | CSV生成、JSONパース |
| `external_api` | 外部API呼び出し | 決済ゲートウェイ等（将来拡張） |
| `validation` | 入力検証 | バリデーションエラー |

## 影響

### ポジティブ

1. **トレーサビリティの向上**: 1リクエストの全ての処理を1JSONで追跡可能
2. **パフォーマンス分析**: ボトルネック特定が容易（各eventのdurationMs）
3. **クエリの簡易化**: `error`フィールドの有無で成功/失敗を即座に判定
4. **モノレポ親和性**: `web-form`, `web-tool` との分散トレーシング基盤に再利用可能
5. **段階的導入**: 既存ログを置き換えず、並行運用が可能

### ネガティブ/リスク

| リスク | 対策 |
|--------|------|
| メモリ増大（大量イベント時） | events配列の上限（100件）を設定。超過時は古いものから破棄 |
| ログサイズ増加（約1.5倍） | 開発環境でのみ有効化、またはサンプリング（1%記録）機能を追加 |
| リクエスト中断時の情報消失 | JVMシャットダウンフックでflush、重要イベントは即座に出力 |
| 機密情報の漏洩 | metadataにはPIIを含めない。必要な場合はマスキング処理を追加 |

## 実装計画

### Phase 1: 基盤実装（1日）

- [ ] `WideEventLog` - データ構造クラス
- [ ] `EventsCollector` - ThreadLocal管理とイベント収集
- [ ] `WideEventLoggingFilter` - Servlet Filter実装
- [ ] `logback-spring.xml` - JSONエンコーダー設定

### Phase 2: 既存ログ移行（1日）

- [ ] `BookController.searchBooks()` → `EventsCollector.record()`
- [ ] `AppExceptionHandler` → `EventsCollector.setError()`
- [ ] `BookDownloadUsecase` → `EventsCollector.record()` / `setError()`

### Phase 3: 検証（1日）

- [ ] ローカル環境でログ出力確認
- [ ] CloudWatch Logs Insightsクエリの検証
- [ ] パフォーマンス影響測定（スループット比較）

### Phase 4: 拡張（将来）

- [ ] MyBatis InterceptorによるSQL実行の自動記録
- [ ] `@Timed`アノテーションによる宣言的計測
- [ ] 分散トレーシング（`web-form`/`web-tool`連携）

## 関連ドキュメント

- [Spring Boot公式: Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [logstash-logback-encoder](https://github.com/logfellow/logstash-logback-encoder)
- 関連コード: `apps/api/src/main/java/dev/ymkz/demo/api/presentation/controller/BookController.java`

## 備考

- 本ADRは日本語で記述（プロジェクト規約に準拠）
- 実装完了後、ステータスを「Accepted」に更新
- 設計変更が発生した場合は、新規ADRを作成し本ADRを「Superseded」に変更
