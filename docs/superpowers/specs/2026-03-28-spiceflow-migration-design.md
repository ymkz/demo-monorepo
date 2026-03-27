# Spiceflow Migration Design: apps/web-tool

**Date:** 2026-03-28
**Status:** Approved

## Overview

`apps/web-tool` を Next.js Pages Router から Spiceflow + Vite RSC フルスタック構成に移行する。APIプロキシ、ページレンダリング、静的ファイル配信をすべてSpiceflowで統合し、UIライブラリをMantineからTailwind CSSに変更する。

## Current State

- **Framework:** Next.js 16 Pages Router (port 4000)
- **UI:** Mantine + nuqs (クエリパラメータ管理)
- **API Client:** @hey-api/openapi-ts でSpring Boot (port 8080) のOpenAPI specから自動生成
- **API Route:** `/api/books/download` → Spring BootへのCSVダウンロードプロキシ
- **Pages:** `index.tsx` のみ (getServerSideProps + AppShell)
- **Issues:** ハードコードされたAPI Token (`"X-API-Token": "TODO"`)

## Target State

- **Framework:** Spiceflow + Vite (RSC対応)
- **UI:** Tailwind CSS
- **Query Validation:** Zod (Spiceflow組み込み)
- **API Proxy:** fetch直呼び (openapi-ts生成クライアント廃止)
- **API Token:** 環境変数 `API_TOKEN` から読み込み

## Architecture

```
apps/web-tool/
├── src/
│   ├── server.ts              # Spiceflowアプリ定義
│   │                          #   - API routes (download proxy)
│   │                          #   - Layout (HTML shell)
│   │                          #   - Page (home page)
│   ├── components/
│   │   └── BookSearch.tsx     # 検索フォーム ('use client')
│   └── styles/
│       └── global.css         # Tailwind CSS エントリ
├── public/                    # 静的ファイル
├── vite.config.ts             # Vite + Spiceflowプラグイン
├── tsconfig.json              # TypeScript設定
├── postcss.config.cjs         # PostCSS (Tailwind用)
├── tailwind.config.ts         # Tailwind CSS設定
└── package.json
```

## Component Design

### Spiceflow Server (`src/server.ts`)

単一ファイルでAPIルートとページを定義:

```typescript
import { Spiceflow, serveStatic } from 'spiceflow'
import { z } from 'zod'

const searchParamsSchema = z.object({
  isbn: z.string().optional(),
  title: z.string().optional(),
  status: z.string().optional(),
  priceFrom: z.coerce.number().optional(),
  priceTo: z.coerce.number().optional(),
  publishedAtStart: z.string().optional(),
  publishedAtEnd: z.string().optional(),
  order: z.string().default('-published_at'),
  offset: z.coerce.number().default(0),
  limit: z.coerce.number().default(20),
})

const app = new Spiceflow()
  // API: CSV download proxy
  .get('/api/books/download', async ({ request }) => {
    const url = new URL('http://localhost:8080/books/download')
    url.search = new URL(request.url).search
    const response = await fetch(url, {
      headers: { 'X-API-Token': process.env.API_TOKEN ?? '' },
    })
    return new Response(response.body, {
      status: response.status,
      headers: {
        'Content-Type': 'text/csv; charset=utf-8',
        'Content-Disposition': 'attachment; filename=books_YYYYMMDD.csv',
      },
    })
  })
  // Layout
  .layout('/*', async ({ children }) => (
    <html lang="ja">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      </head>
      <body class="min-h-screen bg-gray-50">{children}</body>
    </html>
  ))
  // Page: Home
  .page('/', async ({ request }) => {
    const url = new URL(request.url)
    const params = searchParamsSchema.parse(
      Object.fromEntries(url.searchParams)
    )
    const downloadUrl = `/api/books/download?${url.searchParams.toString()}`
    return (
      <div class="flex flex-col min-h-screen">
        <header class="h-[60px] bg-white border-b flex items-center px-4">
          <span>Header</span>
        </header>
        <main class="flex-1 p-4">
          <h1 class="text-2xl font-bold">Page</h1>
          <pre>{JSON.stringify(params, null, 2)}</pre>
          <a href={downloadUrl} class="text-blue-600 underline">download</a>
        </main>
        <footer class="h-[60px] bg-white border-t flex items-center px-4">
          <span>Footer</span>
        </footer>
      </div>
    )
  })
  // Static files
  .use(serveStatic({ root: './public' }))

export default app
export type App = typeof app
```

### Vite Config (`vite.config.ts`)

```typescript
import { spiceflow } from 'spiceflow/vite'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [spiceflow()],
})
```

### TypeScript Config (`tsconfig.json`)

```json
{
  "compilerOptions": {
    "strict": true,
    "noEmit": true,
    "skipLibCheck": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "resolveJsonModule": true,
    "forceConsistentCasingInFileNames": true,
    "jsx": "react-jsx",
    "jsxImportSource": "react",
    "module": "esnext",
    "moduleResolution": "bundler",
    "target": "ES2024",
    "lib": ["dom", "dom.iterable", "esnext"],
    "types": ["vite/client"]
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "vite-env.d.ts"],
  "exclude": ["node_modules"]
}
```

## Dependency Changes

### Remove

| Package | Reason |
|---------|--------|
| next | Spiceflowに置換 |
| @mantine/core | Tailwind CSSに置換 |
| @mantine/hooks | Mantine依存削除 |
| nuqs | Zodバリデーションに置換 |
| @hey-api/openapi-ts | fetch直呼びに置換 |
| postcss-preset-mantine | 不要 |
| postcss-simple-vars | Mantine用、不要 |

### Add

| Package | Version | Reason |
|---------|---------|--------|
| spiceflow | latest (@rscタグ) | フレームワーク |
| react | catalog | RSC用 |
| react-dom | catalog | RSC用 |
| zod | (spiceflow依存) | クエリバリデーション |
| tailwindcss | v4 | UIスタイリング |
| vite | latest | バンドラ |
| @types/react | catalog | 型定義 |
| @types/react-dom | catalog | 型定義 |

## Data Flow

### Before (Next.js)

```
Browser → Next.js SSR (getServerSideProps)
                    ↓ nuqsバリデーション
                    ↓ ページレンダリング
Browser → /api/books/download → Next.js API Route
                    ↓ fetch (ハードコードToken)
                    ↓ pipe
                    Spring Boot :8080
```

### After (Spiceflow)

```
Browser → Spiceflow .page('/') → Zodバリデーション → RSCレンダリング
Browser → /api/books/download → Spiceflow route
                    ↓ fetch (env.API_TOKEN)
                    ↓ Response(body) forwarding
                    Spring Boot :8080
```

## Error Handling

- **Zodバリデーションエラー**: デフォルト値でフォールバック
- **Spring Boot通信エラー**: Spiceflowの `onError` でキャッチして500応答
- **API Token未設定**: 起動時に警告ログ、リクエスト時は空文字で送信

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `API_TOKEN` | Yes | Spring Boot API認証トークン |
| `API_BASE_URL` | No | Spring Boot API URL (default: `http://localhost:8080`) |

## Port

開発サーバーは引き続き **port 4000** で起動するようVite設定で指定。

## Migration Steps

1. 依存関係の入れ替え (package.json)
2. 設定ファイルの新規作成 (vite.config.ts, tailwind, tsconfig, postcss)
3. Spiceflowサーバー実装 (server.ts)
4. 不要ファイルの削除 (pages/, generated/, next.config.ts, openapi-ts.config.ts)
5. Wireitスクリプトの更新
6. 動作確認

## Risks

| Risk | Mitigation |
|------|------------|
| Spiceflow RSCの安定性 | 小規模アプリのため影響範囲限定 |
| Tailwind CSSへのUI移行 | 現状シンプルなレイアウトのみ |
| Mantine依存の他ページの存在 | 現在index.tsxのみで影響なし |
