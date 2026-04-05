# Fumadocs ドキュメントサイト設計書

**作成日**: 2025-04-05  
**対象プロジェクト**: demo-monorepo  
**ステータス**: 設計完了・実装待ち

---

## 1. 概要

### 1.1 目的

社内開発者向けの包括的なドキュメントサイトを構築し、以下を一元管理する：

- APIリファレンス（OpenAPI連携）
- 開発ガイド・チュートリアル
- アーキテクチャ決定記録（ADR）
- データベース設計情報
- 運用手順

### 1.2 背景

現在のドキュメントは `docs/` ディレクトリに分散しており、検索性・閲覧体験に課題がある。Fumadocsを採用することで：

- 既存のNext.js技術スタックを活用
- OpenAPI仕様からの自動生成
- 統一されたナビゲーションと検索

を実現する。

### 1.3 スコープ

**対象リポジトリ**: `/home/ymkz/work/github.com/ymkz/demo-monorepo`

| 対象 | 内容 |
|------|------|
| 新規作成 | `apps/docs/` - Fumadocsプロジェクト |
| 移行対象 | `docs/adr/*` - ADRドキュメント |
| 移行対象 | `docs/database/*` - DBスキーマ情報 |
| 移行対象 | `docs/development/*` - 開発ガイド |
| 移行対象 | `docs/operation/*` - 運用手順 |
| 自動生成 | `apps/api` からOpenAPIドキュメント |

---

## 2. アーキテクチャ

### 2.1 技術スタック

| コンポーネント | 技術 | 理由 |
|--------------|------|------|
| フレームワーク | Next.js 16 (App Router) | 既存プロジェクトと統一 |
| ドキュメントエンジン | Fumadocs Core + UI | Reactベースの柔軟性 |
| コンテンツ形式 | MDX | インタラクティブなコンポーネント埋め込み |
| 検索 | Orama (Fumadocs組み込み) | クライアントサイド全文検索 |
| スタイリング | Tailwind CSS | 既存プロジェクトと統一 |
| パッケージ管理 | pnpm workspace | monorepo統合 |

### 2.2 ディレクトリ構成

```
apps/docs/
├── src/
│   ├── app/
│   │   ├── (docs)/
│   │   │   ├── [[...slug]]/
│   │   │   │   └── page.tsx      # 動的ページレンダリング
│   │   │   └── layout.tsx        # ドキュメントレイアウト
│   │   ├── api/
│   │   │   └── search/route.ts   # 検索APIエンドポイント
│   │   ├── layout.tsx            # ルートレイアウト
│   │   └── page.tsx              # トップページ（リダイレクト）
│   ├── components/
│   │   ├── mermaid.tsx           # Mermaid図表コンポーネント
│   │   └── openapi-client.tsx    # OpenAPIインタラクティブ表示
│   ├── content/
│   │   ├── docs/
│   │   │   ├── api/
│   │   │   │   └── index.mdx     # API概要（自動生成）
│   │   │   ├── guides/
│   │   │   │   ├── setup.mdx     # セットアップガイド
│   │   │   │   ├── testing.mdx   # テストポリシー
│   │   │   │   └── api-development.mdx
│   │   │   ├── adr/
│   │   │   │   ├── 20260313-wide-event-logging.mdx
│   │   │   │   └── 20260329-migrate-web-tool-to-spiceflow.mdx
│   │   │   └── database/
│   │   │       ├── index.mdx
│   │   │       ├── schema/
│   │   │       │   ├── authors.mdx
│   │   │       │   ├── books.mdx
│   │   │       │   └── ...
│   │   │       └── er-diagram.mdx
│   │   └── meta.json             # ナビゲーション構造定義
│   ├── lib/
│   │   ├── source.ts             # Fumadocsソース設定
│   │   └── openapi.ts            # OpenAPI連携ユーティリティ
│   ├── types/
│   │   └── index.ts
│   └── styles/
│       └── globals.css
├── public/
│   └── openapi.json              # ビルド時にコピー
├── next.config.js
├── tailwind.config.js
├── tsconfig.json
└── package.json
```

### 2.3 ナビゲーション構造

```
📚 ドキュメント
├── 🚀 Getting Started
│   ├── プロジェクト概要
│   └── ローカル開発環境構築
├── 📖 Guides
│   ├── API開発ガイド
│   ├── フロントエンド開発ガイド
│   └── テストポリシー
├── 🔌 API Reference
│   ├── 概要
│   └── エンドポイント一覧（自動生成）
├── 🗄️ Database
│   ├── スキーマ概要
│   ├── ER図
│   └── テーブル詳細
└── 🏛️ ADR
    ├── ワイドイベントロギング
    └── Spiceflow移行
```

---

## 3. コンテンツ移行計画

### 3.1 移行マッピング

| 現在地 | 移行先 | 備考 |
|--------|--------|------|
| `docs/development/setup-local.md` | `src/content/docs/guides/setup.mdx` | フロントマター追加 |
| `docs/development/develop-on-local.md` | `src/content/docs/guides/local-dev.mdx` | 統合または分割 |
| `docs/development/testing-policy.md` | `src/content/docs/guides/testing.mdx` | フロントマター追加 |
| `docs/adr/*.md` | `src/content/docs/adr/*.mdx` | 日付プレフィックス維持 |
| `docs/database/schema/*.md` | `src/content/docs/database/schema/*.mdx` | リンク・画像調整 |
| `docs/database/schema/*.svg` | `public/images/schema/*.svg` | パス調整 |

### 3.2 MDX変換ルール

既存のMarkdownをMDXに変換する際のルール：

```yaml
# フロントマター追加例
---
title: "ワイドイベントロギングの導入"
description: "リクエスト単位でのログ集約によるトレーサビリティ向上"
date: 2026-03-13
status: accepted
tags: ["logging", "architecture"]
---
```

### 3.3 OpenAPI連携仕様

```typescript
// src/lib/openapi.ts
interface OpenAPIConfig {
  // APIアプリケーションのOpenAPIスペックパス
  sourcePath: "../../api/build/openapi.json";
  // ドキュメント出力設定
  outputDir: "./src/content/docs/api";
  // 自動生成ページのテンプレート
  template: "fumadocs-openapi";
}
```

ビルド時に以下を実行：
1. `./gradlew :apps:api:generateOpenApiDocs` - OpenAPIスペック生成
2. Fumadocsがスペックを読み込み、ページを動的生成

---

## 4. コンポーネント設計

### 4.1 Mermaid図表コンポーネント

```tsx
// src/components/mermaid.tsx
interface MermaidProps {
  chart: string;
  caption?: string;
}

// 使用例
<Mermaid
  chart={`
    erDiagram
      AUTHORS ||--o{ BOOKS : writes
      BOOKS ||--|| BOOK_STATUS : has
  `}
  caption="データベースER図"
/>
```

### 4.2 データベーススキーマ表示コンポーネント

既存の`docs/database/schema/*.md`を統一的に表示するためのコンポーネント：

```tsx
// src/components/db-schema.tsx
interface DBSchemaProps {
  tableName: string;
  columns: ColumnDef[];
  relationships: Relationship[];
}
```

### 4.3 ADR表示コンポーネント

ADR固有のメタデータ（ステータス、日付、影響範囲）を視覚的に表示：

```tsx
// src/components/adr-header.tsx
interface ADRHeaderProps {
  status: 'proposed' | 'accepted' | 'deprecated' | 'superseded';
  date: string;
  deciders: string[];
  supersededBy?: string;
}
```

---

## 5. ビルド・デプロイ

### 5.1 開発モード

```bash
# ドキュメントのみ起動
pnpm --filter docs dev

# 全アプリ並行起動（API + web-form + web-tool + docs）
pnpm dev
```

### 5.2 本番ビルド

```bash
# OpenAPIスペック生成
./gradlew :apps:api:generateOpenApiDocs

# ドキュメントビルド
pnpm --filter docs build

# 出力: apps/docs/dist/
```

### 5.3 Wireit統合

`apps/docs/package.json`にWireit設定を追加：

```json
{
  "scripts": {
    "build": "wireit",
    "generate-openapi": "wireit"
  },
  "wireit": {
    "generate-openapi": {
      "command": "./gradlew :apps:api:generateOpenApiDocs",
      "files": ["../../api/src/**/*"],
      "output": ["../../api/build/openapi.json"]
    },
    "build": {
      "dependencies": ["generate-openapi"],
      "command": "next build",
      "files": ["src/**/*", "../../api/build/openapi.json"],
      "output": ["dist/**"]
    }
  }
}
```

---

## 6. 制約と考慮事項

### 6.1 技術制約

| 制約 | 対応策 |
|------|--------|
| Next.js 16.1.6対応 | Fumadocsの互換性バージョンを確認 |
| React 19.2.4対応 | peerDependenciesの確認 |
| Biome使用 | ESLint/Prettier設定を除外 |

### 6.2 既存資産との整合性

- **`.agents/skills/`**: スキルドキュメントは現状維持、必要に応じてFumadocsからリンク
- **`README.md`**: ルートREADMEは簡潔に保ち、詳細はドキュメントサイトへ誘導

### 6.3 将来の拡張性

- **国際化(i18n)**: Fumadocsのi18n機能を使用可能
- **バージョニング**: リリースバージョンごとのドキュメント分離
- **認証**: 必要に応じてNext.jsのMiddlewareで実装可能

---

## 7. 実装チェックリスト

### Phase 1: セットアップ
- [ ] `apps/docs/` プロジェクト作成
- [ ] Fumadocs Core + UI インストール
- [ ] Tailwind CSS設定統合
- [ ] tsconfig/biome設定統合

### Phase 2: コンテンツ移行
- [ ] 開発ガイド移行（`docs/development/*`）
- [ ] ADR移行（`docs/adr/*`）
- [ ] データベースドキュメント移行（`docs/database/*`）
- [ ] Mermaidコンポーネント実装

### Phase 3: OpenAPI連携
- [ ] OpenAPIスペック生成タスク設定
- [ ] Fumadocs OpenAPIプラグイン設定
- [ ] APIリファレンスページ自動生成確認

### Phase 4: 最適化
- [ ] 検索インデックス最適化
- [ ] ナビゲーション構造調整
- [ ] レスポンシブ対応確認
- [ ] パフォーマンス最適化

---

## 8. 参考資料

- [Fumadocs 公式ドキュメント](https://fumadocs.vercel.app/)
- [Next.js ドキュメント](https://nextjs.org/docs)
- [既存プロジェクト構成](/home/ymkz/work/github.com/ymkz/demo-monorepo/AGENTS.md)

---

## 9. 承認履歴

| 日付 | 承認者 | 内容 |
|------|--------|------|
| 2025-04-05 | ymkz | 設計承認・実装着手許可 |
