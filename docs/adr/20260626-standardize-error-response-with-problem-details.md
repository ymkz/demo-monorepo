# ADR 003: Problem Detailsによるエラーレスポンス標準化

## ステータス

**Accepted** - 2026-06-26

## コンテキスト

`apps/api` では、APIエラーの仕様として独自の `ErrorResponse(code, message)` を使っていた。
しかし実装上は `ResponseEntityExceptionHandler` が返すSpring MVC標準のエラーと、個別の `@ExceptionHandler` が返す独自レスポンスが混在していた。

この状態では、クライアントがエラーを扱うときにレスポンス構造を一つに固定できない。
OpenAPI上も独自の `ErrorResponse` を参照していたため、実際のSpring Boot 4系のエラー表現とAPI契約がずれる余地があった。

Spring Frameworkは `org.springframework.http.ProblemDetail` を提供している。
HTTP APIのエラー表現としては RFC 9457 の Problem Details が現在の標準であり、メディアタイプは `application/problem+json` を使う。

## 決定

APIのエラーレスポンスを **Problem Details** に寄せる。

標準フィールドは以下を使う。

- `type`
- `title`
- `status`
- `detail`
- `instance`

アプリケーション固有のエラーコードは、標準フィールドではなく拡張メンバー `errorCode` として返す。
バリデーションエラーの詳細は、レスポンス全体を代表する単一のProblem Detailsに対して、拡張メンバー `errors` として返す。

### 採用する設計

- `spring.mvc.problemdetails.enabled=true` を有効にする
- `AppExceptionHandler` は `ResponseEntityExceptionHandler` を継承したまま、個別例外も `ProblemDetail` で返す
- `Content-Type` は `application/problem+json` に統一する
- `errorCode` は既存の `AppEvent` の `code()` を使う
- `detail` はクライアントに出してよい説明だけに限定する
- 内部例外のメッセージやスタックトレースはレスポンスへ出さず、ログにのみ残す
- バリデーションエラーは `errors[]` に `{ field, message }` を入れる
- OpenAPIでは `ProblemDetailResponse` をエラーschemaとして参照する
- フロントエンドのエラー表示は `detail`、`title`、`errorCode` の順で利用する

## レスポンス例

バリデーションエラーでは、代表する問題を `title` と `detail` で表し、個別の項目を `errors` に入れる。

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "リクエストボディの検証に失敗しました",
  "instance": "/books",
  "errorCode": "dw1001",
  "errors": [
    {
      "field": "isbn",
      "message": "空白は許可されていません"
    }
  ]
}
```

## 検討した代替案

| 代替案 | 評価 |
|--------|------|
| 独自 `ErrorResponse(code, message)` を維持する | 既存クライアントへの影響は小さいが、Spring標準例外とAPI契約が揃わない |
| 独自レスポンスにProblem Details相当のフィールドを追加する | 標準メディアタイプやSpringの標準処理を活かしにくい |
| `ProblemDetail` に一括移行する | Spring標準とOpenAPI契約を揃えられるため採用する |

## 影響

### ポジティブ

1. APIエラーのレスポンス形式が `application/problem+json` に統一される
2. Spring MVC標準例外とアプリケーション例外のレスポンス構造を揃えられる
3. クライアントは `detail` や `title` を共通の入口として扱える
4. `errorCode` を拡張メンバーとして残すため、既存のアプリケーション固有分類を維持できる
5. OpenAPIから生成されるTypeScript型も `ProblemDetailResponse` に統一される

### ネガティブ / リスク

| リスク | 対策 |
|--------|------|
| 既存の `code` / `message` を読むクライアントが壊れる | OpenAPI生成型と `web-tool` のエラー処理を更新する |
| `detail` に内部情報を出すと情報漏洩につながる | 例外メッセージをそのまま返さず、固定の説明文を使う |
| `errors[]` の形がRFCの標準フィールドではない | RFC 9457の拡張メンバーとして扱い、OpenAPIに明記する |
| `about:blank` だけでは問題種別の識別力が弱い | 将来、公開可能なエラーカタログURIを用意した時点で `type` を置き換える |

## OpenAPI生成方針

OpenAPI生成元は、公開用の静的 `openapi.json` ではなくSpringdocの `/v3/api-docs` とする。
静的ファイルを生成元にすると、実装変更がOpenAPIに反映されず、古い仕様を再取得してしまう。

生成された `apps/api/src/main/resources/static/openapi/openapi.json` は公開用仕様として引き続きリポジトリに保持する。
フロントエンドの `apps/*/src/generated/` はOpenAPIから再生成される一時成果物として扱う。

## 実装サマリー

- 追加: `apps/api/src/main/java/dev/ymkz/demo/api/shared/exception/ProblemDetailResponse.java`
- 追加: `apps/api/src/main/java/dev/ymkz/demo/api/shared/exception/ValidationErrorResponse.java`
- 変更: `apps/api/src/main/java/dev/ymkz/demo/api/shared/exception/AppExceptionHandler.java`
- 変更: `apps/api/src/main/java/dev/ymkz/demo/api/features/books/BookController.java`
- 変更: `apps/api/src/main/resources/application.yaml`
- 変更: `apps/api/build.gradle.kts`
- 変更: `apps/api/src/main/resources/static/openapi/openapi.json`
- 変更: `apps/web-tool/src/routes/index.tsx`
- 削除: `apps/api/src/main/java/dev/ymkz/demo/api/shared/exception/ErrorResponse.java`

## 検証

- `./gradlew :apps:api:spotlessCheck`
- `./gradlew :apps:api:test`
- `./gradlew :apps:api:intTest`
- `./gradlew :apps:api:build`
- `pnpm --filter web-tool typecheck`
- `pnpm --filter web-tool build`
- `pnpm lint`
- `pnpm typecheck`

## 関連ドキュメント

- RFC 9457: Problem Details for HTTP APIs
- Spring Framework: `ProblemDetail`
- Springdoc OpenAPI生成方針: `docs/development/openapi-policy.md`
