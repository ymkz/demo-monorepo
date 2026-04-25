# ドキュメント

> **注意**: 詳細なドキュメントはドキュメントサイトを参照してください。
> http://localhost:5000 (開発時)

このディレクトリには以下のドキュメントが含まれています：

- `adr/` - アーキテクチャ決定記録（Architecture Decision Records）
- `database/` - データベース設計情報（スキーマ、ER図）
- `development/` - 開発ガイド（セットアップ、テストポリシー等）
  - [OpenAPI 運用方針](./development/openapi-policy.md)

## ドキュメントサイトについて

ドキュメントサイトは `apps/docs/` でFumadocsを使用して構築されています。
ビルド時にこのディレクトリの内容が移行・変換されます。

### ドキュメントサイトの起動

```bash
pnpm --filter @demo/docs dev
```

### ドキュメントの追加・編集

1. `apps/docs/content/docs/` 以下にMDXファイルを作成
2. フロントマター（title, description）を追加
3. 必要に応じて `meta.json` でナビゲーション順序を調整
4. `pnpm check` で確認
5. コミット

## 移行済みドキュメント

| 元ファイル | 移行先 |
|-----------|--------|
| `docs/development/*.md` | `apps/docs/content/docs/guides/*.mdx` |
| `docs/adr/*.md` | `apps/docs/content/docs/adr/*.mdx` |
| `docs/database/schema/*.md` | `apps/docs/content/docs/database/schema/*.mdx` |
