# OpenAPI 運用方針

このプロジェクトはコードファーストで OpenAPI 仕様を生成する。

## OpenAPI 仕様ファイル

`apps/api/src/main/resources/static/openapi/` は生成物だが、例外的にリポジトリへコミットする。

理由は、Spring Boot の static resource として OpenAPI ドキュメントをデプロイし、外部から参照できる状態にするため。通常のビルド出力は `build/` 配下に置くが、このディレクトリだけは公開ドキュメントとして扱う。

OpenAPI 仕様を更新する場合は、API 実装を変更したうえで `./gradlew build` を実行し、生成された `openapi.json` を確認してコミットする。

## フロントエンド生成クライアント

`apps/web-form/src/generated/` と `apps/web-tool/src/generated/` はコミットしない。

これらは `@hey-api/openapi-ts` により `apps/api/src/main/resources/static/openapi/openapi.json` から生成される一時成果物であり、`pnpm check`、`pnpm build`、`pnpm typecheck` の前段で再生成される。

生成クライアントに差分が必要な場合は、生成後の TypeScript ファイルを直接編集せず、バックエンドの API 実装または OpenAPI アノテーションを修正する。
