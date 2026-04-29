# PROJECT KNOWLEDGE BASE

**Commit:** ced8292 | **Branch:** main

## OVERVIEW
Hybrid monorepo: Spring Boot (Kotlin) backend + TypeScript frontends. OpenAPI-first, MySQL persistence.

## STRUCTURE
```
apps/
├── api/         # Spring Boot REST (MyBatis + MySQL)
├── core/        # Shared domain library
├── web-form/    # TanStack Start (port 3000)
└── web-tool/    # TanStack Start (port 4000)
```

## ANTI-PATTERNS (禁止事項)

| 禁止パターン | 理由 |
|-------------|------|
| ハードコードされた秘密情報 | `apps/web-tool/src/pages/api/books/download.ts`で発見 |
| コミット内のTODOコメント | Issueを作成すること |
| フロントエンドでSpiceflowを再導入 | TanStack Startへ移行済み |
| `src`内のビルド出力 | ただし`apps/api/src/main/resources/static/openapi/`はドキュメント公開用の例外 |

## UNIQUE STYLES

- **日本語必須**: すべてのドキュメントと応答は日本語
- **テスト名**: 日本語使用（例: `enumValues_期待される全ての値が存在すること`）
- **Biome**使用: ESLint/Prettier禁止
- **TanStack Start** + **React 19.2.5**

## COMMANDS

```bash
pnpm dev              # フロントエンド起動 (3000, 4000)
./gradlew bootRun     # Spring Boot起動
pnpm build            # フロントエンドビルド
./gradlew build       # バックエンドビルド（テスト含む）
./gradlew intTest     # 統合テスト（TestContainers + MySQL）
pnpm check            # lint + typecheck + build
```

## INSPECTION PROTOCOL

**検証がパスするまでは作業は完了していない。**

- コード変更後、完了報告やPR作成の前に必ず検証コマンドを実行する
- 「ビルドは通ると思います」のような未検証の完了宣言は禁止
- 検証に失敗した場合は原因を修正し、同じ検証を再実行してパスを確認する
- どの検証を実行すべきか迷う場合は、対象範囲で実行可能な最も包括的なチェックを選ぶ
- 検証できない事情がある場合は、実行できなかったコマンドと理由を明示する

### Gradle Backend Verification

```bash
./gradlew :apps:<name>:spotlessCheck  # フォーマットチェック
./gradlew :apps:<name>:test           # 単体テスト
./gradlew :apps:<name>:intTest        # 統合テスト（存在する場合）
./gradlew :apps:<name>:build          # ビルド
./gradlew :apps:<name>:check          # 推奨: 包括チェック
```

自動修正が必要な場合:

```bash
./gradlew :apps:<name>:spotlessApply
./gradlew :apps:<name>:spotlessCheck
```

### pnpm Frontend Verification

```bash
pnpm lint       # リントチェック
pnpm typecheck  # 型チェック
pnpm build      # ビルド
pnpm test       # テスト
pnpm check      # 推奨: 包括チェック
```

自動修正が必要な場合:

```bash
pnpm format
pnpm lint --write
```

### Project-Specific Verification

```bash
./gradlew :apps:api:spotlessCheck
./gradlew :apps:api:test
./gradlew :apps:api:intTest
./gradlew :apps:api:build
./gradlew :apps:core:check
pnpm --filter web-form check
pnpm --filter web-tool check
```

## GOTCHAS

- Wireit使用: 個別コマンドではなく`pnpm dev`を使用
- 日本語応答必須（`.github/copilot-instructions.md`参照）
- OpenAPI specはコードファーストで生成するが、ドキュメントとしてデプロイするため`apps/api/src/main/resources/static/openapi/`をリポジトリにコミット
- フロントエンドの`apps/*/src/generated/`はOpenAPIから生成する一時成果物のためコミットしない
- `apps/core`はGradleモジュールのみ（Next.jsから不可）
