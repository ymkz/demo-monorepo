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
| src内のビルド出力 | `static/openapi/`は`build/`に属すべき |

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

## GOTCHAS

- Wireit使用: 個別コマンドではなく`pnpm dev`を使用
- 日本語応答必須（`.github/copilot-instructions.md`参照）
- OpenAPI specはビルド成果物だがリポジトリにコミット
- `apps/core`はGradleモジュールのみ（Next.jsから不可）
