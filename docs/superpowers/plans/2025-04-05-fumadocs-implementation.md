# Fumadocsドキュメントサイト実装プラン

> **For agentic workers:** REQUIRED SUB-SKILL: Use @superpowers:subagent-driven-development (recommended) or @superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fumadocsを使用したドキュメントサイトを構築し、既存ドキュメントを移行・統合する

**Architecture:** Next.js 16 + Fumadocs Core + App Router。既存のpnpm workspaceに追加し、OpenAPI自動生成・Mermaid図表・全文検索を統合。

**Tech Stack:** Next.js 16.1.6, React 19.2.4, Fumadocs, TypeScript, Tailwind CSS, pnpm, Wireit

**設計書参照:** `docs/superpowers/specs/2025-04-05-fumadocs-documentation-design.md`

---

## Phase 1: プロジェクトセットアップ

### Task 1: Fumadocsプロジェクト作成

**Files:**
- Create: `apps/docs/package.json`
- Create: `apps/docs/next.config.ts`
- Create: `apps/docs/tsconfig.json`
- Create: `apps/docs/tailwind.config.ts`
- Create: `apps/docs/biome.json`
- Create: `apps/docs/.gitignore`

**前提条件:**
- 既存プロジェクトのNext.jsバージョンは16.1.6
- 既存プロジェクトのReactバージョンは19.2.4
- Biomeを使用（ESLint/Prettier不使用）

- [ ] **Step 1: apps/docsディレクトリ作成とpackage.json作成**

```bash
mkdir -p apps/docs
```

```json
// apps/docs/package.json
{
  "name": "@demo/docs",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev -p 5000",
    "build": "wireit",
    "start": "next start",
    "check": "biome check .",
    "check:fix": "biome check --write .",
    "typecheck": "tsc --noEmit",
    "generate-openapi": "wireit"
  },
  "wireit": {
    "generate-openapi": {
      "command": "cd ../.. && ./gradlew :apps:api:generateOpenApiDocs --quiet || echo 'OpenAPI generation skipped'",
      "files": ["../../api/src/**/*"],
      "output": ["../../api/build/openapi.json"]
    },
    "build": {
      "dependencies": ["generate-openapi"],
      "command": "next build",
      "files": ["src/**/*", "../../api/build/openapi.json"],
      "output": [".next/**", "dist/**"]
    }
  },
  "dependencies": {
    "next": "catalog:",
    "react": "catalog:",
    "react-dom": "catalog:",
    "fumadocs-core": "^14.0.0",
    "fumadocs-ui": "^14.0.0",
    "fumadocs-mdx": "^11.0.0",
    "fumadocs-openapi": "^5.0.0",
    "mermaid": "^11.0.0",
    "tailwindcss": "catalog:"
  },
  "devDependencies": {
    "@biomejs/biome": "catalog:",
    "@types/node": "catalog:",
    "@types/react": "catalog:",
    "@types/react-dom": "catalog:",
    "typescript": "catalog:",
    "wireit": "catalog:"
  }
}
```

- [ ] **Step 2: tsconfig.json作成**

```json
// apps/docs/tsconfig.json
{
  "extends": "../../tsconfig.json",
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    },
    "plugins": [
      {
        "name": "next"
      }
    ]
  },
  "include": [
    "next.config.ts",
    "next-env.d.ts",
    "src/**/*.ts",
    "src/**/*.tsx",
    "content/**/*.mdx",
    ".next/types/**/*.ts"
  ],
  "exclude": ["node_modules", ".next", "dist"]
}
```

- [ ] **Step 3: next.config.ts作成**

```typescript
// apps/docs/next.config.ts
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "export",
  distDir: "dist",
  images: {
    unoptimized: true,
  },
};

export default nextConfig;
```

- [ ] **Step 4: tailwind.config.ts作成**

```typescript
// apps/docs/tailwind.config.ts
import { createPreset } from "fumadocs-ui/tailwind-plugin";
import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/**/*.{js,ts,jsx,tsx,mdx}",
    "./content/**/*.{md,mdx}",
    "./node_modules/fumadocs-ui/dist/**/*.js",
  ],
  presets: [createPreset()],
};

export default config;
```

- [ ] **Step 5: biome.json作成**

```json
// apps/docs/biome.json
{
  "extends": ["../../biome.jsonc"],
  "files": {
    "include": ["src/**/*", "content/**/*"],
    "ignore": [".next", "dist", "node_modules"]
  }
}
```

- [ ] **Step 6: .gitignore作成**

```
// apps/docs/.gitignore
.next/
dist/
node_modules/
*.log
```

- [ ] **Step 7: 依存関係インストール**

```bash
cd apps/docs
pnpm install
```

- [ ] **Step 8: コミット**

```bash
git add apps/docs/package.json apps/docs/tsconfig.json apps/docs/next.config.ts apps/docs/tailwind.config.ts apps/docs/biome.json apps/docs/.gitignore
git commit -m "chore(docs): Fumadocsプロジェクト初期セットアップ"
```

---

### Task 2: Fumadocs基本構造セットアップ

**Files:**
- Create: `apps/docs/src/app/layout.tsx`
- Create: `apps/docs/src/app/page.tsx`
- Create: `apps/docs/src/app/(docs)/layout.tsx`
- Create: `apps/docs/src/app/(docs)/[[...slug]]/page.tsx`
- Create: `apps/docs/src/lib/source.ts`
- Create: `apps/docs/src/styles/globals.css`
- Create: `apps/docs/content/docs/meta.json`

- [ ] **Step 1: グローバルCSS作成**

```css
/* apps/docs/src/styles/globals.css */
@tailwind base;
@tailwind components;
@tailwind utilities;
```

- [ ] **Step 2: ルートレイアウト作成**

```tsx
// apps/docs/src/app/layout.tsx
import "./globals.css";
import { RootProvider } from "fumadocs-ui/provider";
import type { ReactNode } from "react";

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="ja" suppressHydrationWarning>
      <body className="flex flex-col min-h-screen">
        <RootProvider>{children}</RootProvider>
      </body>
    </html>
  );
}
```

- [ ] **Step 3: トップページ作成（ドキュメントへリダイレクト）**

```tsx
// apps/docs/src/app/page.tsx
import { redirect } from "next/navigation";

export default function HomePage() {
  redirect("/docs");
}
```

- [ ] **Step 4: Fumadocsソース設定作成**

```typescript
// apps/docs/src/lib/source.ts
import { docs } from "@/.source";
import { loader } from "fumadocs-core/source";

export const source = loader({
  baseUrl: "/docs",
  source: docs.toFumadocsSource(),
});
```

- [ ] **Step 5: ドキュメントレイアウト作成**

```tsx
// apps/docs/src/app/(docs)/layout.tsx
import { DocsLayout } from "fumadocs-ui/layout";
import type { ReactNode } from "react";
import { source } from "@/lib/source";

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <DocsLayout
      tree={source.pageTree}
      nav={{
        title: "Demo Monorepo Docs",
      }}
    >
      {children}
    </DocsLayout>
  );
}
```

- [ ] **Step 6: 動的ページ作成**

```tsx
// apps/docs/src/app/(docs)/[[...slug]]/page.tsx
import { source } from "@/lib/source";
import {
  DocsPage,
  DocsBody,
  DocsDescription,
  DocsTitle,
} from "fumadocs-ui/page";
import type { Metadata } from "next";
import { notFound } from "next/navigation";

export default async function Page(props: {
  params: Promise<{ slug?: string[] }>;
}) {
  const params = await props.params;
  const page = source.getPage(params.slug);

  if (!page) {
    notFound();
  }

  const MDX = page.data.body;

  return (
    <DocsPage toc={page.data.toc} full={page.data.full}>
      <DocsTitle>{page.data.title}</DocsTitle>
      <DocsDescription>{page.data.description}</DocsDescription>
      <DocsBody>
        <MDX />
      </DocsBody>
    </DocsPage>
  );
}

export async function generateStaticParams() {
  return source.generateParams();
}

export async function generateMetadata(props: {
  params: Promise<{ slug?: string[] }>;
}) {
  const params = await props.params;
  const page = source.getPage(params.slug);

  if (!page) return {};

  return {
    title: page.data.title,
    description: page.data.description,
  } satisfies Metadata;
}
```

- [ ] **Step 7: ナビゲーション構造定義**

```json
// apps/docs/content/docs/meta.json
{
  "title": "ドキュメント",
  "pages": [
    "index",
    "---ガイド---",
    "guides",
    "---API---",
    "api",
    "---データベース---",
    "database",
    "---アーキテクチャ---",
    "adr"
  ]
}
```

- [ ] **Step 8: インデックスページ作成**

```mdx
---
title: "Demo Monorepo ドキュメント"
description: "社内開発者向けドキュメントサイト"
---

# Demo Monorepo ドキュメント

このサイトでは、プロジェクトの開発ガイド、APIリファレンス、アーキテクチャ決定記録などを提供します。

## クイックスタート

- [開発環境のセットアップ](/docs/guides/setup)
- [APIリファレンス](/docs/api)
- [データベース設計](/docs/database)

## プロジェクト構成

このモノレポは以下のアプリケーションで構成されています：

| アプリ | 説明 | ポート |
|--------|------|--------|
| `apps/api` | Spring Boot REST API | 8080 |
| `apps/web-form` | Next.js App Router フロントエンド | 3000 |
| `apps/web-tool` | Next.js Pages Router フロントエンド | 4000 |
| `apps/docs` | ドキュメントサイト（本サイト） | 5000 |
```

- [ ] **Step 9: コミット**

```bash
git add apps/docs/src/
git add apps/docs/content/
git commit -m "feat(docs): Fumadocs基本構造セットアップ"
```

---

## Phase 2: 既存ドキュメント移行

### Task 3: 開発ガイド移行

**Files:**
- Create: `apps/docs/content/docs/guides/meta.json`
- Create: `apps/docs/content/docs/guides/setup.mdx`
- Create: `apps/docs/content/docs/guides/local-dev.mdx`
- Create: `apps/docs/content/docs/guides/testing.mdx`

**参照元:**
- `docs/development/setup-local.md`
- `docs/development/develop-on-local.md`
- `docs/development/testing-policy.md`

- [ ] **Step 1: ガイドセクションmeta.json作成**

```json
// apps/docs/content/docs/guides/meta.json
{
  "title": "開発ガイド",
  "pages": ["setup", "local-dev", "testing"]
}
```

- [ ] **Step 2: セットアップガイド移行**

```mdx
---
title: "開発環境セットアップ"
description: "ローカル開発環境の構築手順"
---

# 開発環境セットアップ

## 前提条件

以下のツールがインストールされていることを確認してください：

- Node.js 20+
- pnpm
- Java 21+
- Docker & Docker Compose

## インストール手順

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd demo-monorepo
```

### 2. 依存関係のインストール

```bash
# Node.js依存関係
pnpm install

# Gradleラッパーに実行権限付与（Linux/macOS）
chmod +x gradlew
```

### 3. 環境変数の設定

```bash
cp .env.example .env
# 必要に応じて.envを編集
```

### 4. データベース起動

```bash
docker-compose up -d mysql
```

### 5. アプリケーション起動

```bash
# フロントエンド（別ターミナルで）
pnpm dev

# バックエンド（別ターミナルで）
./gradlew bootRun
```

## 確認

- web-form: http://localhost:3000
- web-tool: http://localhost:4000
- API: http://localhost:8080
- ドキュメント: http://localhost:5000
```

- [ ] **Step 3: ローカル開発ガイド移行**

```mdx
---
title: "ローカル開発ガイド"
description: "日々の開発作業での推奨手法"
---

# ローカル開発ガイド

## 開発ワークフロー

### ブランチ戦略

- `main`: 本番環境
- `feature/*`: 機能開発
- `fix/*`: バグ修正

### コミットメッセージ

```
feat: 新機能追加
fix: バグ修正
docs: ドキュメント更新
refactor: リファクタリング
test: テスト追加・修正
chore: その他
```

## コード品質

### チェックコマンド

```bash
# フロントエンド
pnpm check          # lint + typecheck
pnpm check:fix      # 自動修正

# バックエンド
./gradlew spotlessCheck   # フォーマットチェック
./gradlew spotlessApply   # 自動修正
```

### テスト実行

```bash
# フロントエンド
pnpm test

# バックエンド
./gradlew test      # 単体テスト
./gradlew intTest   # 統合テスト
```

## トラブルシューティング

### ポート競合

`.env`でポートを変更：

```env
WEB_FORM_PORT=3001
WEB_TOOL_PORT=4001
API_PORT=8081
DOCS_PORT=5001
```

### データベース接続エラー

```bash
# MySQLコンテナ再起動
docker-compose restart mysql

# データ初期化（注意：データが消えます）
docker-compose down -v
docker-compose up -d mysql
```
```

- [ ] **Step 4: テストポリシー移行**

```mdx
---
title: "テストポリシー"
description: "テスト戦略と実装ガイドライン"
---

# テストポリシー

## テストピラミッド

```
      /\
     /  \     E2E（少数）
    /____\
   /      \   統合テスト（中程度）
  /________\
 /          \ 単体テスト（多数）
/____________\
```

## 単体テスト

### フロントエンド

- Vitest使用
- コンポーネントテストはReact Testing Library
- モックはMSW（API）またはvitest mock

```bash
pnpm test
```

### バックエンド

- JUnit 5 + Kotest（Kotlin）
- モックはMockK

```bash
./gradlew test
```

## 統合テスト

### TestContainers使用

実際のMySQLコンテナを使用した統合テスト：

```bash
./gradlew intTest
```

## E2Eテスト

### Playwright使用

```bash
pnpm e2e
```

## カバレッジ目標

| レベル | 目標 | 必須 |
|--------|------|------|
| 単体テスト | 80%+ | 重要ビジネスロジック |
| 統合テスト | 主要フロー | APIエンドポイント |
| E2E | クリティカルパス | ユーザージャーニー |

## テスト命名規則

日本語使用推奨：

```kotlin
@Test
fun `有効なISBNの場合、書籍が正常に登録されること`() {
    // ...
}
```

```typescript
it('無効な入力の場合、エラーメッセージが表示されること', () => {
  // ...
});
```
```

- [ ] **Step 5: コミット**

```bash
git add apps/docs/content/docs/guides/
git commit -m "docs(docs): 開発ガイド移行"
```

---

### Task 4: ADR移行

**Files:**
- Create: `apps/docs/content/docs/adr/meta.json`
- Create: `apps/docs/content/docs/adr/20260313-wide-event-logging.mdx`
- Create: `apps/docs/content/docs/adr/20260329-migrate-web-tool-to-spiceflow.mdx`

**参照元:**
- `docs/adr/20260313-wide-event-logging.md`
- `docs/adr/20260329-migrate-web-tool-to-spiceflow.md`

- [ ] **Step 1: ADRセクションmeta.json作成**

```json
// apps/docs/content/docs/adr/meta.json
{
  "title": "アーキテクチャ決定記録 (ADR)",
  "pages": [
    "20260313-wide-event-logging",
    "20260329-migrate-web-tool-to-spiceflow"
  ]
}
```

- [ ] **Step 2: ADRヘッダーコンポーネント作成（オプション）**

```tsx
// apps/docs/src/components/adr-header.tsx
interface ADRHeaderProps {
  status: "proposed" | "accepted" | "deprecated" | "superseded";
  date: string;
  deciders?: string[];
}

export function ADRHeader({ status, date, deciders }: ADRHeaderProps) {
  const statusColors = {
    proposed: "bg-yellow-100 text-yellow-800",
    accepted: "bg-green-100 text-green-800",
    deprecated: "bg-gray-100 text-gray-800",
    superseded: "bg-red-100 text-red-800",
  };

  return (
    <div className="mb-6 p-4 bg-muted rounded-lg">
      <div className="flex flex-wrap gap-4 text-sm">
        <div>
          <span className="font-semibold">ステータス:</span>{" "}
          <span className={`px-2 py-1 rounded ${statusColors[status]}`}>
            {status}
          </span>
        </div>
        <div>
          <span className="font-semibold">決定日:</span> {date}
        </div>
        {deciders && (
          <div>
            <span className="font-semibold">決定者:</span>{" "}
            {deciders.join(", ")}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: ADRコンテンツ移行**

既存のADR MarkdownファイルをMDXに変換し、フロントマターを追加：

```mdx
---
title: "ワイドイベントロギングの導入"
description: "リクエスト単位でのログ集約によるトレーサビリティ向上"
date: 2026-03-13
status: accepted
---

# ADR-001: ワイドイベントロギングの導入

## 背景

（既存内容をここに）

## 決定

（既存内容をここに）

## 影響

（既存内容をここに）
```

- [ ] **Step 4: コミット**

```bash
git add apps/docs/content/docs/adr/
git add apps/docs/src/components/adr-header.tsx
git commit -m "docs(docs): ADR移行と表示コンポーネント追加"
```

---

## Phase 3: データベースドキュメント・Mermaid統合

### Task 5: Mermaidコンポーネント実装

**Files:**
- Create: `apps/docs/src/components/mermaid.tsx`
- Modify: `apps/docs/src/app/(docs)/layout.tsx`

- [ ] **Step 1: Mermaidコンポーネント作成**

```tsx
// apps/docs/src/components/mermaid.tsx
"use client";

import { useEffect, useRef, useState } from "react";
import mermaid from "mermaid";

interface MermaidProps {
  chart: string;
  caption?: string;
}

export function Mermaid({ chart, caption }: MermaidProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [svg, setSvg] = useState<string>("");

  useEffect(() => {
    mermaid.initialize({
      startOnLoad: false,
      theme: "default",
    });

    if (ref.current) {
      mermaid.render("mermaid-svg", chart.trim()).then(({ svg }) => {
        setSvg(svg);
      });
    }
  }, [chart]);

  return (
    <figure className="my-6">
      <div
        ref={ref}
        className="overflow-x-auto bg-muted p-4 rounded-lg"
        dangerouslySetInnerHTML={{ __html: svg }}
      />
      {caption && (
        <figcaption className="text-center text-sm text-muted-foreground mt-2">
          {caption}
        </figcaption>
      )}
    </figure>
  );
}
```

- [ ] **Step 2: MDXコンポーネントマッピング設定**

```tsx
// apps/docs/src/mdx-components.tsx
import { Mermaid } from "@/components/mermaid";
import defaultMdxComponents from "fumadocs-ui/mdx";
import type { MDXComponents } from "mdx/types";

export function getMDXComponents(): MDXComponents {
  return {
    ...defaultMdxComponents,
    Mermaid,
  };
}
```

- [ ] **Step 3: コミット**

```bash
git add apps/docs/src/components/mermaid.tsx
git add apps/docs/src/mdx-components.tsx
git commit -m "feat(docs): Mermaid図表コンポーネント実装"
```

---

### Task 6: データベースドキュメント移行

**Files:**
- Create: `apps/docs/content/docs/database/meta.json`
- Create: `apps/docs/content/docs/database/index.mdx`
- Create: `apps/docs/content/docs/database/schema/meta.json`
- Create/Modify: `apps/docs/content/docs/database/schema/*.mdx`
- Create: `apps/docs/content/docs/database/er-diagram.mdx`

**参照元:**
- `docs/database/README.md`
- `docs/database/schema/*.md`
- `docs/database/schema/*.svg`

- [ ] **Step 1: データベースセクションmeta.json作成**

```json
// apps/docs/content/docs/database/meta.json
{
  "title": "データベース",
  "pages": ["index", "er-diagram", "schema"]
}
```

- [ ] **Step 2: データベース概要ページ作成**

```mdx
---
title: "データベース設計"
description: "データベース構成と設計方針"
---

# データベース設計

## 概要

このプロジェクトではMySQLを使用しています。

## テーブル一覧

| テーブル | 説明 |
|----------|------|
| authors | 著者情報 |
| books | 書籍情報 |
| publishers | 出版社情報 |
| book_status | 書籍ステータスマスタ |

## ER図

詳細なER図は[ER図ページ](/docs/database/er-diagram)を参照してください。
```

- [ ] **Step 3: ER図ページ作成（Mermaid使用）**

```mdx
---
title: "ER図"
description: "データベースエンティティ関係図"
---

# ER図

import { Mermaid } from "@/components/mermaid";

<Mermaid
  chart={`\nerDiagram\n  AUTHORS ||--o{ BOOKS : writes\n  PUBLISHERS ||--o{ BOOKS : publishes\n  BOOK_STATUS ||--|| BOOKS : has\n\n  AUTHORS {\n    bigint id PK\n    varchar name\n    varchar email\n    datetime created_at\n    datetime updated_at\n  }\n\n  PUBLISHERS {\n    bigint id PK\n    varchar name\n    varchar address\n    datetime created_at\n    datetime updated_at\n  }\n\n  BOOK_STATUS {\n    int id PK\n    varchar name\n    varchar description\n  }\n\n  BOOKS {\n    bigint id PK\n    varchar title\n    varchar isbn\n    bigint author_id FK\n    bigint publisher_id FK\n    int status_id FK\n    datetime published_at\n    datetime created_at\n    datetime updated_at\n  }\n`}
  caption="データベースER図"
/>
```

- [ ] **Step 4: スキーマセクションmeta.json作成**

```json
// apps/docs/content/docs/database/schema/meta.json
{
  "title": "テーブル定義",
  "pages": ["authors", "books", "publishers", "book_status"]
}
```

- [ ] **Step 5: テーブル定義ページ作成（既存から移行）**

既存の`docs/database/schema/*.md`をMDXに変換し、フロントマターを追加。
画像（SVG）は`public/images/schema/`にコピーし、パスを調整。

```mdx
---
title: "authors テーブル"
description: "著者情報テーブル定義"
---

# authors テーブル

## 概要

著者情報を管理するテーブルです。

## カラム定義

| カラム名 | 型 | NULL | 説明 |
|----------|------|------|------|
| id | bigint | NO | 主キー |
| name | varchar(255) | NO | 著者名 |
| email | varchar(255) | YES | メールアドレス |
| created_at | datetime | NO | 作成日時 |
| updated_at | datetime | NO | 更新日時 |

## インデックス

| インデックス名 | カラム | 種別 |
|----------------|--------|------|
| PRIMARY | id | 主キー |
| idx_email | email | 一意 |

## 関連テーブル

- `books` - 1対多（著者が複数の書籍を持つ）
```

- [ ] **Step 6: SVG画像をpublicにコピー**

```bash
mkdir -p apps/docs/public/images/schema
cp docs/database/schema/*.svg apps/docs/public/images/schema/
```

- [ ] **Step 7: コミット**

```bash
git add apps/docs/content/docs/database/
git add apps/docs/public/images/schema/
git commit -m "docs(docs): データベースドキュメント移行とMermaid統合"
```

---

## Phase 4: OpenAPI連携

### Task 7: OpenAPI連携設定

**Files:**
- Create: `apps/docs/src/lib/openapi.ts`
- Create: `apps/docs/content/docs/api/meta.json`
- Create: `apps/docs/content/docs/api/index.mdx`
- Modify: `apps/docs/package.json`（scripts追加）

**前提:**
- `apps/api`にOpenAPI生成タスクが設定済みであること

- [ ] **Step 1: OpenAPIユーティリティ作成**

```typescript
// apps/docs/src/lib/openapi.ts
import type { OpenAPIObject } from "openapi3-ts/oas31";

export async function getOpenAPISpec(): Promise<OpenAPIObject | null> {
  try {
    const spec = await import("@/../public/openapi.json");
    return spec.default || spec;
  } catch {
    return null;
  }
}

export function generateEndpointDocs(spec: OpenAPIObject) {
  const paths = Object.entries(spec.paths || {});
  
  return paths.map(([path, methods]) => ({
    path,
    methods: Object.entries(methods || {}).map(([method, operation]) => ({
      method: method.toUpperCase(),
      summary: operation?.summary || "",
      description: operation?.description || "",
      parameters: operation?.parameters || [],
      requestBody: operation?.requestBody,
      responses: operation?.responses || {},
    })),
  }));
}
```

- [ ] **Step 2: APIセクションmeta.json作成**

```json
// apps/docs/content/docs/api/meta.json
{
  "title": "APIリファレンス",
  "pages": ["index"]
}
```

- [ ] **Step 3: API概要ページ作成**

```mdx
---
title: "APIリファレンス"
description: "REST APIエンドポイント一覧"
---

# APIリファレンス

## 概要

Spring Bootで構築されたREST APIの仕様です。

## ベースURL

```
http://localhost:8080
```

## 認証

（認証方式が決定次第追記）

## エンドポイント一覧

OpenAPI仕様から自動生成されるエンドポイント一覧はビルド後に表示されます。

## OpenAPI仕様

生のOpenAPI JSONは以下から取得できます：

```bash
curl http://localhost:8080/v3/api-docs
```
```

- [ ] **Step 4: ビルドスクリプト確認**

package.jsonのWireit設定が以下を満たしていることを確認：

1. `generate-openapi`タスクで`./gradlew :apps:api:generateOpenApiDocs`を実行
2. `build`タスクが`generate-openapi`に依存
3. OpenAPI JSONが`public/openapi.json`に出力される

- [ ] **Step 5: コミット**

```bash
git add apps/docs/src/lib/openapi.ts
git add apps/docs/content/docs/api/
git commit -m "feat(docs): OpenAPI連携基盤実装"
```

---

## Phase 5: Wireit統合・最終調整

### Task 8: Wireit統合と検証

**Files:**
- Modify: `package.json`（root）
- Modify: `pnpm-workspace.yaml`
- Modify: `apps/docs/package.json`

- [ ] **Step 1: ワークスペース設定確認**

`pnpm-workspace.yaml`に`apps/docs`が含まれていることを確認：

```yaml
# pnpm-workspace.yaml
packages:
  - "apps/*"
```

- [ ] **Step 2: ルートpackage.json確認**

`package.json`のscriptsにdocs関連があれば追加：

```json
{
  "scripts": {
    "dev": "wireit",
    "build": "wireit",
    "check": "wireit"
  },
  "wireit": {
    "dev": {
      "command": "pnpm -r --parallel dev"
    },
    "build": {
      "dependencies": ["./apps/docs:build", "./apps/api:build", "./apps/web-form:build", "./apps/web-tool:build"]
    },
    "check": {
      "dependencies": ["./apps/docs:check", "./apps/api:check", "./apps/web-form:check", "./apps/web-tool:check"]
    }
  }
}
```

- [ ] **Step 3: 開発サーバー起動テスト**

```bash
# ドキュメントのみ起動
pnpm --filter docs dev

# 全アプリ起動（別ターミナルでAPIも起動が必要）
pnpm dev
```

期待される出力：
- Docsサーバーがポート5000で起動
- `http://localhost:5000`にアクセスしてトップページが表示される

- [ ] **Step 4: ビルドテスト**

```bash
# OpenAPI生成を含むビルド
pnpm --filter docs build
```

期待される出力：
- `apps/docs/dist/`に静的ファイルが生成される
- エラーなしで完了

- [ ] **Step 5: 型チェック**

```bash
pnpm --filter docs typecheck
```

期待される出力：
- エラーなし

- [ ] **Step 6: リントチェック**

```bash
pnpm --filter docs check
```

期待される出力：
- エラーなし

- [ ] **Step 7: コミット**

```bash
git add package.json pnpm-workspace.yaml apps/docs/package.json
git commit -m "chore(docs): Wireit統合とビルド設定"
```

---

## Phase 6: 既存ドキュメント統合

### Task 9: 既存ドキュメントの整理

**Files:**
- Create: `docs/README.md`
- Modify: 既存ドキュメントへのリンク追加

- [ ] **Step 1: 既存docs/README.md作成/更新**

```markdown
# ドキュメント

> **注意**: 詳細なドキュメントはドキュメントサイトを参照してください。
> http://localhost:5000 (開発時)

このディレクトリには以下のドキュメントが含まれています：

- `adr/` - アーキテクチャ決定記録
- `database/` - データベース設計情報
- `development/` - 開発ガイド
- `operation/` - 運用手順

## ドキュメントサイトについて

ドキュメントサイトは `apps/docs/` でFumadocsを使用して構築されています。
ビルド時にこのディレクトリの内容が移行・変換されます。
```

- [ ] **Step 2: ルートREADME.md更新（オプション）**

ルートREADMEにドキュメントサイトへのリンクを追加：

```markdown
## ドキュメント

詳細なドキュメントは [ドキュメントサイト](./apps/docs/) を参照してください。
```

- [ ] **Step 3: コミット**

```bash
git add docs/README.md
git commit -m "docs: 既存ドキュメントにドキュメントサイトへの参照を追加"
```

---

## 実装完了チェックリスト

- [ ] Phase 1: プロジェクトセットアップ完了
  - [ ] Fumadocsインストール
  - [ ] TypeScript/Tailwind設定
  - [ ] 基本ページ構造作成
- [ ] Phase 2: 既存ドキュメント移行完了
  - [ ] 開発ガイド移行
  - [ ] ADR移行
- [ ] Phase 3: データベースドキュメント完了
  - [ ] Mermaid統合
  - [ ] スキーマ情報移行
- [ ] Phase 4: OpenAPI連携完了
  - [ ] OpenAPI連携設定
- [ ] Phase 5: Wireit統合完了
  - [ ] ビルド設定
  - [ ] 開発サーバー動作確認
- [ ] Phase 6: ドキュメント統合完了
  - [ ] 既存ドキュメントへの参照追加

---

## トラブルシューティング

### ビルドエラー

**問題**: `Cannot find module '@/.source'`
**解決**: `content`ディレクトリにMDXファイルが存在し、開発サーバーまたはビルドを実行していることを確認

**問題**: OpenAPI JSONが見つからない
**解決**: `./gradlew :apps:api:generateOpenApiDocs`が成功しているか確認

### 開発サーバー問題

**問題**: ポート5000が使用中
**解決**: `apps/docs/package.json`のdevスクリプトでポートを変更

### 型エラー

**問題**: Fumadocs関連の型エラー
**解決**: `node_modules`を削除して`pnpm install`を再実行
