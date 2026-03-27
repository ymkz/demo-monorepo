# Spiceflow Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `apps/web-tool` を Next.js Pages Router から Spiceflow + Vite RSC フルスタック構成に移行する。

**Architecture:** SpiceflowのRSC機能（`.page()` + `.layout()`）を使い、ViteをバンドラとしてAPIルートとページレンダリングを統合。APIプロキシはSpiceflowルートで実装し、UIはTailwind CSSに移行。

**Tech Stack:** Spiceflow (RSC), Vite 8, React 19, Tailwind CSS 4, Zod 4

**Spec:** `docs/superpowers/specs/2026-03-28-spiceflow-migration-design.md`

---

## File Structure

```
apps/web-tool/
├── app/
│   ├── main.tsx              # Spiceflowエントリポイント (API + pages + layouts)
│   └── globals.css           # Tailwind CSS エントリ
├── public/                   # 静的ファイル (既存)
├── vite.config.ts            # Vite + Spiceflowプラグイン (新規)
├── tsconfig.json             # TypeScript設定 (更新)
├── package.json              # 依存関係更新
├── .env                      # 環境変数 (API_TOKEN等)
└── AGENTS.md                 # 更新
```

### 削除対象

| ファイル/ディレクトリ | 理由 |
|---|---|
| `src/pages/` | Next.js Pages Router廃止 |
| `src/generated/` | openapi-ts生成コード廃止 |
| `next.config.ts` | Next.js廃止 |
| `openapi-ts.config.ts` | openapi-ts廃止 |
| `postcss.config.cjs` | Tailwind v4はViteプラグインのみで動作 |
| `next-env.d.ts` | Next.js廃止 |

---

### Task 1: 依存関係の更新

**Files:**
- Modify: `apps/web-tool/package.json`

- [ ] **Step 1: package.jsonを更新**

```json
{
  "name": "web-tool",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "engines": {
    "node": ">=24"
  },
  "scripts": {
    "dev": "wireit",
    "build": "wireit",
    "typecheck": "wireit",
    "start": "node dist/rsc/index.js"
  },
  "wireit": {
    "dev": {
      "command": "vite dev --port 4000",
      "service": true
    },
    "build": {
      "command": "vite build --app"
    },
    "typecheck": {
      "command": "tsc --noEmit"
    }
  },
  "dependencies": {
    "@tailwindcss/vite": "^4.2.2",
    "react": "catalog:",
    "react-dom": "catalog:",
    "spiceflow": "latest",
    "tailwindcss": "^4.0.6",
    "zod": "^3.25.0"
  },
  "devDependencies": {
    "@types/react": "catalog:",
    "@types/react-dom": "catalog:",
    "@vitejs/plugin-react": "^6.0.1",
    "vite": "^8.0.0"
  }
}
```

注意: SpiceflowのpeerDependencyが`zod ^4.0.0`だが、npmの最新安定版に合わせて調整が必要。インストール後に確認。

- [ ] **Step 2: 依存関係をインストール**

Run: `pnpm install`
Expected: エラーなくインストール完了。SpiceflowのpeerDependency警告を確認。

- [ ] **Step 3: コミット**

```bash
git add apps/web-tool/package.json pnpm-lock.yaml
git commit -m "chore(web-tool): Spiceflow移行に向けた依存関係を更新"
```

---

### Task 2: 設定ファイルの作成

**Files:**
- Create: `apps/web-tool/vite.config.ts`
- Modify: `apps/web-tool/tsconfig.json`
- Create: `apps/web-tool/.env`

- [ ] **Step 1: vite.config.tsを作成**

```typescript
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { spiceflowPlugin } from 'spiceflow/vite'
import { defineConfig } from 'vite'

export default defineConfig({
  clearScreen: false,
  plugins: [
    spiceflowPlugin({
      entry: './app/main.tsx',
    }),
    react(),
    tailwindcss(),
  ],
})
```

- [ ] **Step 2: tsconfig.jsonを更新**

```json
{
  "compilerOptions": {
    "target": "es5",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "forceConsistentCasingInFileNames": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "noImplicitAny": false,
    "types": ["vite/client"]
  },
  "include": ["**/*.ts", "**/*.tsx", "vite-env.d.ts"],
  "exclude": ["node_modules", "dist"]
}
```

- [ ] **Step 3: .envを作成**

```
API_TOKEN=
API_BASE_URL=http://localhost:8080
```

- [ ] **Step 4: コミット**

```bash
git add apps/web-tool/vite.config.ts apps/web-tool/tsconfig.json apps/web-tool/.env
git commit -m "chore(web-tool): Spiceflow用設定ファイルを追加"
```

---

### Task 3: Spiceflowエントリポイントの作成

**Files:**
- Create: `apps/web-tool/app/main.tsx`
- Create: `apps/web-tool/app/globals.css`

- [ ] **Step 1: app/globals.cssを作成**

```css
@import 'tailwindcss';
```

- [ ] **Step 2: app/main.tsxを作成**

```tsx
import './globals.css'
import { Spiceflow, serveStatic } from 'spiceflow'
import { Head, ProgressBar } from 'spiceflow/react'
import { Suspense } from 'react'
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

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080'

export const app = new Spiceflow()
  .use(serveStatic({ root: './public' }))
  .get('/api/books/download', async ({ request }) => {
    const url = new URL('/books/download', API_BASE_URL)
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
  .layout('/*', async ({ children }) => {
    return (
      <html lang="ja">
        <Head>
          <meta charSet="utf-8" />
          <meta name="robots" content="noindex" />
          <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0, viewport-fit=cover"
          />
          <Head.Title>Web Tool</Head.Title>
        </Head>
        <body className="min-h-screen bg-gray-50">
          <ProgressBar />
          {children}
        </body>
      </html>
    )
  })
  .page('/', async function Home({ request }) {
    const searchParams = new URL(request.url).searchParams
    const parsed = searchParamsSchema.safeParse(
      Object.fromEntries(searchParams),
    )
    const params = parsed.success ? parsed.data : searchParamsSchema.parse({})
    const downloadUrl = `/api/books/download?${searchParams.toString()}`

    return (
      <div className="flex min-h-screen flex-col">
        <header className="flex h-[60px] items-center border-b bg-white px-4">
          <div>Header</div>
        </header>
        <main className="flex-1 p-4">
          <h1 className="text-2xl font-bold">Page</h1>
          <pre className="mt-4 rounded bg-gray-100 p-4 text-sm">
            {JSON.stringify(params, null, 2)}
          </pre>
          <a
            href={downloadUrl}
            className="mt-4 inline-block text-blue-600 underline"
          >
            download
          </a>
        </main>
        <footer className="flex h-[60px] items-center border-t bg-white px-4">
          <div>Footer</div>
        </footer>
      </div>
    )
  })

app.listen(Number(process.env.PORT || 4000))
```

- [ ] **Step 3: 型チェックでエラーがないことを確認**

Run: `pnpm --filter web-tool typecheck`
Expected: エラーなし

- [ ] **Step 4: 開発サーバーが起動することを確認**

Run: `pnpm --filter web-tool dev`
Expected: `http://localhost:4000` でページが表示される

- [ ] **Step 5: コミット**

```bash
git add apps/web-tool/app/
git commit -m "feat(web-tool): Spiceflowエントリポイントを実装"
```

---

### Task 4: 旧ファイルの削除

**Files:**
- Delete: `apps/web-tool/src/` (pages, generated)
- Delete: `apps/web-tool/next.config.ts`
- Delete: `apps/web-tool/openapi-ts.config.ts`
- Delete: `apps/web-tool/postcss.config.cjs`
- Delete: `apps/web-tool/next-env.d.ts`

- [ ] **Step 1: 旧ファイルを削除**

```bash
rm -rf apps/web-tool/src apps/web-tool/next.config.ts apps/web-tool/openapi-ts.config.ts apps/web-tool/postcss.config.cjs apps/web-tool/next-env.d.ts
```

- [ ] **Step 2: コミット**

```bash
git add -A apps/web-tool/
git commit -m "chore(web-tool): Next.js関連ファイルを削除"
```

---

### Task 5: AGENTS.mdの更新

**Files:**
- Modify: `apps/web-tool/AGENTS.md`

- [ ] **Step 1: AGENTS.mdを更新**

```markdown
# WEB-TOOL APP

**Updated:** 2026-03-28
**Parent:** /AGENTS.md

## OVERVIEW
Spiceflow + Vite RSC アプリケーション。Tailwind CSSを使用。

## STRUCTURE
```
apps/web-tool/
├── app/
│   ├── main.tsx              # Spiceflowエントリ (API + pages + layouts)
│   └── globals.css           # Tailwind CSS
├── public/                   # 静的ファイル
├── vite.config.ts            # Vite + Spiceflowプラグイン
└── package.json
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Pages & Layouts | `app/main.tsx` | `.page()` と `.layout()` |
| API Routes | `app/main.tsx` | `.get()`, `.post()` 等 |
| Styles | `app/globals.css` | Tailwind CSS |

## COMMANDS

```bash
pnpm --filter web-tool dev       # 開発サーバー (port 4000)
pnpm --filter web-tool build     # プロダクションビルド
pnpm --filter web-tool typecheck # 型チェック
```

## CONVENTIONS

- Spiceflow `.page()` / `.layout()` でRSCページ定義
- クライアントコンポーネントは `'use client'` ディレクティブ
- Zodでquery/bodyバリデーション
- Tailwind CSSでスタイリング
```

- [ ] **Step 2: コミット**

```bash
git add apps/web-tool/AGENTS.md
git commit -m "docs(web-tool): AGENTS.mdをSpiceflow構成に更新"
```

---

### Task 6: ビルドと動作確認

- [ ] **Step 1: プロダクションビルドを実行**

Run: `pnpm --filter web-tool build`
Expected: エラーなくビルド完了。`dist/rsc/index.js` が生成される。

- [ ] **Step 2: 型チェックを実行**

Run: `pnpm --filter web-tool typecheck`
Expected: エラーなし

- [ ] **Step 3: ビルド成果物でサーバー起動確認**

Run: `cd apps/web-tool && node dist/rsc/index.js`
Expected: `http://localhost:4000` でアクセス可能

- [ ] **Step 4: 最終コミット（必要に応じて修正）**

ビルドや型チェックで問題があれば修正してコミット。
