---
name: wide-event-logging
description: このプロジェクトでは1リクエストあたりの全イベントを1つのJSONログに集約する「ワイドイベントロギング」を採用している。MDCとThreadLocalを組み合わせ、リクエスト処理のトレーサビリティ向上とパフォーマンス分析を実現する。
license: Proprietary
compatibility: Spring Boot 3.x, Java 17+, logstash-logback-encoder
metadata:
  author: ymkz
  version: "2.0"
  language: java
---

# Wide Event Logging 方針

## 核心理念

1リクエストの全イベント（DBクエリ、業務処理、エラー等）を**1つのJSONログ**に集約し、以下を実現する：

- **トレーサビリティ**: リクエストIDで関連ログを即座に特定
- **パフォーマンス分析**: 処理時間・イベント発生箇所の可視化
- **構造化ログ**: JSON形式でログ基盤（Grafana Loki等）と連携

## 実装方針

### アーキテクチャ

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────┐
│  Filter (MDC)   │────→│ EventsCollector  │────→│  JSON出力   │
│  requestId設定   │     │  (ThreadLocal)   │     │  レスポンス時  │
└─────────────────┘     └──────────────────┘     └─────────────┘
         ↑                                               │
         └───────────────────────────────────────────────┘
              各レイヤーで EventsCollector.record() 呼び出し
```

### データフロー

1. **リクエスト開始時**（Filter）
   - `EventsCollector.initialize()` でコンテキスト初期化
   - `MDC.put("requestId")` で全ログにrequestIdを紐付け

2. **処理中**（Controller/Usecase/Domain）
   - `EventsCollector.record()` で業務イベントを記録
   - `EventsCollector.setError()` でエラー情報を設定

3. **レスポンス時**（Filter finally）
   - `EventsCollector.finalizeLog()` で集約されたデータを取得
   - JSON形式で一括出力
   - `EventsCollector.clear()` でThreadLocalを必ずクリア

## 記録すべき情報

### 推奨するイベント

| レイヤー | 記録タイミング | 例 |
|---------|--------------|-----|
| Controller | 処理開始/終了 | `book_search_executed` |
| Usecase | 業務ロジック実行 | `payment_processed` |
| Domain | 重要な状態変化 | `order_status_changed` |
| Infrastructure | DBクエリ実行 | `book_select_executed` |
| ErrorHandler | 例外発生時 | （自動的に記録） |

### メタデータ設計

```java
// 良い例：シンプルで検索しやすい構造
EventsCollector.record("book_search_executed", 
    Map.of("totalResults", 42, "queryType", "advanced"));

// 避けるべき例：循環参照・大きすぎるオブジェクト
EventsCollector.record("book_search_executed", bookEntity);  // NG
```

### 命名規約

- **イベントメッセージ**: `snake_case` で動詞終わり
  - ✅ `book_search_executed`
  - ✅ `payment_processing_started`
  - ❌ `bookSearch`（camelCase）
  - ❌ `SEARCH_BOOK`（定数スタイル）

## 実装上の注意点

### ✅ 推奨パターン

1. **即時return前の記録**
   ```java
   EventsCollector.record("validation_failed", 
       Map.of("field", fieldName, "reason", errorReason));
   return ResponseEntity.badRequest().body(error);
   ```

2. **エラー情報の設定**
   ```java
   try {
       processPayment(order);
   } catch (PaymentException ex) {
       EventsCollector.setError(ex, Map.of("orderId", orderId));
       throw ex;  // 再スローしてハンドラに任せる
   }
   ```

3. **メタデータの最小化**
   - 必要最小限の情報のみ記録
   - PII（個人識別情報）は含めない
   - 大きなオブジェクトはIDや統計値に絞る

### ❌ アンチパターン

| パターン | 問題 | 対応 |
|---------|------|------|
| JPAエンティティをmetadataに渡す | 循環参照でシリアライズ失敗 | IDや必要なフィールドのみ抽出 |
| 大量のイベント記録（1000件以上） | ログサイズ肥大・遅延 | サンプリングまたは集計して記録 |
| 個人情報をmetadataに含める | プライバシー侵害 | マスキングまたは除外 |
| 例外を握りつぶして記録 | エラー隠蔽 | 記録後に再スロー |
| @Async内でrecord呼び出し | ThreadLocalが別スレッド | 別のトレーシング方式を検討 |

## テスト方針

### 必須テスト

1. **ThreadLocalクリア確認**
   - 正常終了時にclear()が呼ばれること
   - 例外発生時もclear()が呼ばれること

2. **ログ出力確認**
   - 期待するイベントが含まれること
   - JSON構造が正しいこと

### テスト実装例

```java
@Test
void 正常終了時にEventsCollectorのThreadLocalがクリアされること() {
    // given
    doAnswer(invocation -> {
        assertThat(EventsCollector.getRequestId()).isNotEmpty();
        return null;
    }).when(chain).doFilter(any(), any());

    // when
    filter.doFilterInternal(request, response, chain);

    // then
    assertThat(EventsCollector.getRequestId()).isEmpty();
}
```

## ログ基盤連携

### Grafana Loki (LogQL) クエリ例

```logql
# エラーリクエスト抽出
{app="demo-api"} | json | msg="WIDE_EVENT" | severity="ERROR"

# 特定イベントを含むリクエスト
{app="demo-api"} | json | msg="WIDE_EVENT"
| line_format "{{.events}}" |~ "book_search_executed"

# 処理時間が閾値を超えたリクエスト
{app="demo-api"} | json | msg="WIDE_EVENT" | durationMs > 1000
```

### CloudWatch Logs Insights

```sql
-- エラーリクエスト抽出
fields @timestamp, requestId, path, statusCode
| filter severity = "ERROR"
| sort @timestamp desc

-- 平均処理時間の集計
fields durationMs, path
| stats avg(durationMs) by path
| sort avg(durationMs) desc
```

## 制約・制限事項

| 項目 | 制約 | 備考 |
|------|------|------|
| **Async処理** | 非対応 | @Asyncなど別スレッドではThreadLocalが分離される |
| **イベント数** | 無制限（推奨：100件以下） | 過多の場合はログサイズに注意 |
| **metadataサイズ** | 制限なし（推奨：1KB以下） | 大きすぎるとシリアライズ遅延 |
| **スレッドセーフ** | 対応済み | ThreadLocal使用により自動的に保証 |

## 参考資料

- ADR: `docs/adr/20260313-wide-event-logging.md`
- 実装: `apps/api/src/main/java/dev/ymkz/demo/api/infrastructure/logging/`
- テスト: `apps/api/src/test/java/dev/ymkz/demo/api/infrastructure/logging/WideEventLoggingFilterTest.java`
