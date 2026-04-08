# TanStack Start Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `apps/web-form` と `apps/web-tool` を Spiceflow から TanStack Start へ移行し、既存の画面・URL・ダウンロード機能・build/test 成立を維持する。

**Architecture:** 2つの既存アプリは統合せず、それぞれを独立した TanStack Start アプリへ載せ替える。`web-form` は `/` のみを持つ最小構成、`web-tool` は `/` と `/books/download` を持つ構成へ変換し、`/books/download` は TanStack Start の server route で backend へプロキシする。テストは TanStack ランタイムに強く依存しない純粋関数・純粋コンポーネントを優先して追加し、既存の `pnpm test` を通す。

**Tech Stack:** TanStack Start, TanStack Router, React 19, Vite 8, TypeScript, Zod, Vitest, Tailwind CSS, pnpm, Wireit

**設計書参照:** `docs/superpowers/specs/2026-04-08-tanstack-start-migration-design.md`

---

## 事前メモ

- 現在のベースラインでは `apps/web-form:test` / `apps/web-tool:test` が `No test files found` で失敗する
- この計画では各アプリに最低1件以上のテストを追加し、`pnpm test` が成功する状態にする
- TanStack Start では `vite.config.ts` の plugin 順序が重要。`tanstackStart()` を先、`react()` を後に置く
- route tree は自動生成されるため、`routeTree.gen.ts` は手書きしない

## ファイル構成方針

### web-form

- 維持: `apps/web-form/src/components/layout.tsx`, `apps/web-form/src/routes/index.tsx`, `apps/web-form/src/style.css`, `apps/web-form/src/logger.ts`
- 新規: `apps/web-form/src/router.tsx`, `apps/web-form/src/routes/__root.tsx`, `apps/web-form/src/routes/index.test.tsx`
- 削除: `apps/web-form/src/index.tsx`, `apps/web-form/src/middleware/logger.ts`
- 更新: `apps/web-form/package.json`, `apps/web-form/vite.config.ts`, `apps/web-form/tsconfig.json`

### web-tool

- 維持: `apps/web-tool/src/components/layout.tsx`, `apps/web-tool/src/style.css`, `apps/web-tool/src/logger.ts`
- 新規: `apps/web-tool/src/router.tsx`, `apps/web-tool/src/routes/__root.tsx`, `apps/web-tool/src/lib/search-params.ts`, `apps/web-tool/src/lib/download-proxy.ts`, `apps/web-tool/src/lib/search-params.test.ts`, `apps/web-tool/src/lib/download-proxy.test.ts`, `apps/web-tool/src/routes/index.tsx`, `apps/web-tool/src/routes/books/download.tsx`
- 削除: `apps/web-tool/src/index.tsx`, `apps/web-tool/src/middleware/logger.ts`, `apps/web-tool/src/routes/error.ts`, `apps/web-tool/src/routes/books/download.ts`
- 更新: `apps/web-tool/package.json`, `apps/web-tool/vite.config.ts`, `apps/web-tool/tsconfig.json`

### ワークスペース

- 更新: `pnpm-workspace.yaml`

---

## Task 1: ワークスペース依存関係と web-form の TanStack Start 土台を作る

**Files:**
- Modify: `pnpm-workspace.yaml`
- Modify: `apps/web-form/package.json`
- Modify: `apps/web-form/vite.config.ts`
- Modify: `apps/web-form/tsconfig.json`
- Create: `apps/web-form/src/router.tsx`
- Create: `apps/web-form/src/routes/__root.tsx`

- [ ] **Step 1: TanStack Start 用の catalog を追加する**

```yaml
# pnpm-workspace.yaml
packages:
  - apps/web-form
  - apps/web-tool

catalog:
  "@biomejs/biome": 2.4.9
  "@hey-api/openapi-ts": 0.94.5
  "@tailwindcss/vite": 4.1.0
  "@tanstack/react-router": 1.168.10
  "@tanstack/react-start": 1.167.16
  "@tanstack/router-plugin": 1.167.12
  "@types/node": 24.12.0
  "@types/react": 19.2.14
  "@types/react-dom": 19.2.3
  "@vitejs/plugin-react": 6.0.1
  "@vitest/coverage-v8": 4.1.2
  knip: 5.88.1
  pino: 10.3.1
  react: 19.2.4
  react-dom: 19.2.4
  spiceflow: 1.18.0-rsc.18
  tailwindcss: 4.1.0
  temporal-polyfill-lite: 0.3.3
  typescript: 5.9.3
  vite: 8.0.7
  vitest: 4.1.2
  wireit: 0.14.12
  zod: 4.3.6

catalogMode: strict

injectWorkspacePackages: true

managePackageManagerVersions: true

syncInjectedDepsAfterScripts:
  - build

useNodeVersion: 25.6.1
```

- [ ] **Step 2: web-form の package.json を TanStack Start 構成へ更新する**

```json
{
  "name": "web-form",
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
    "test": "wireit",
    "coverage": "wireit",
    "generate:openapi": "wireit"
  },
  "wireit": {
    "generate:openapi": {
      "command": "openapi-ts"
    },
    "dev": {
      "command": "vite dev",
      "service": true,
      "dependencies": [
        "generate:openapi"
      ]
    },
    "build": {
      "command": "vite build",
      "dependencies": [
        "generate:openapi"
      ]
    },
    "typecheck": {
      "command": "tsc --noEmit",
      "dependencies": [
        "generate:openapi"
      ]
    },
    "test": {
      "command": "vitest --run",
      "dependencies": [
        "generate:openapi"
      ]
    },
    "coverage": {
      "command": "vitest --run --coverage",
      "dependencies": [
        "generate:openapi"
      ]
    }
  },
  "dependencies": {
    "@tanstack/react-router": "catalog:",
    "@tanstack/react-start": "catalog:",
    "react": "catalog:",
    "react-dom": "catalog:",
    "pino": "catalog:",
    "tailwindcss": "catalog:",
    "temporal-polyfill-lite": "catalog:",
    "zod": "catalog:"
  },
  "devDependencies": {
    "@hey-api/openapi-ts": "catalog:",
    "@tailwindcss/vite": "catalog:",
    "@tanstack/router-plugin": "catalog:",
    "@types/react": "catalog:",
    "@types/react-dom": "catalog:",
    "@vitejs/plugin-react": "catalog:",
    "@vitest/coverage-v8": "catalog:",
    "vitest": "catalog:",
    "vite": "catalog:"
  }
}
```

- [ ] **Step 3: web-form の Vite 設定を TanStack Start 用に差し替える**

```ts
// apps/web-form/vite.config.ts
import tailwindcss from "@tailwindcss/vite";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
	clearScreen: false,
	server: { port: 3000 },
	plugins: [tanstackStart(), react(), tailwindcss()],
});
```

- [ ] **Step 4: web-form の tsconfig を route tree 生成に対応させる**

```json
// apps/web-form/tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noEmit": true,
    "allowJs": true,
    "incremental": true,
    "skipLibCheck": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "resolveJsonModule": true,
    "noUncheckedIndexedAccess": false,
    "forceConsistentCasingInFileNames": true,
    "jsx": "react-jsx",
    "module": "esnext",
    "moduleResolution": "bundler",
    "target": "ES2024",
    "lib": ["dom", "dom.iterable", "esnext"]
  },
  "include": ["**/*.ts", "**/*.tsx"],
  "exclude": ["node_modules", ".tanstack", ".output"]
}
```

- [ ] **Step 5: web-form の router 定義を追加する**

```ts
// apps/web-form/src/router.tsx
import { createRouter } from "@tanstack/react-router";
import { routeTree } from "./routeTree.gen";

export function getRouter() {
	return createRouter({
		routeTree,
		scrollRestoration: true,
	});
}

declare module "@tanstack/react-router" {
	interface Register {
		router: ReturnType<typeof getRouter>;
	}
}
```

- [ ] **Step 6: web-form の root route を追加する**

```tsx
// apps/web-form/src/routes/__root.tsx
/// <reference types="vite/client" />
import { HeadContent, Outlet, Scripts, createRootRoute } from "@tanstack/react-router";
import appCss from "../style.css?url";

export const Route = createRootRoute({
	head: () => ({
		meta: [
			{ charSet: "utf-8" },
			{ name: "robots", content: "noindex" },
			{ name: "viewport", content: "width=device-width, initial-scale=1.0, viewport-fit=cover" },
			{ title: "フォーム" },
		],
		links: [{ rel: "stylesheet", href: appCss }],
	}),
	component: RootComponent,
});

function RootComponent() {
	return (
		<html lang="ja">
			<head>
				<HeadContent />
			</head>
			<body className="min-h-screen">
				<Outlet />
				<Scripts />
			</body>
		</html>
	);
}
```

- [ ] **Step 7: route tree が生成されることを確認する**

Run: `pnpm --filter web-form typecheck`

Expected: `apps/web-form/src/routeTree.gen.ts` が自動生成され、typecheck が成功する

- [ ] **Step 8: コミット**

```bash
git add pnpm-workspace.yaml apps/web-form/package.json apps/web-form/vite.config.ts apps/web-form/tsconfig.json apps/web-form/src/router.tsx apps/web-form/src/routes/__root.tsx apps/web-form/src/routeTree.gen.ts
git commit -m "chore(web-form): scaffold TanStack Start app"
```

---

## Task 2: web-form の `/` を移植し、最初のテストを追加する

**Files:**
- Modify: `apps/web-form/src/routes/index.tsx`
- Create: `apps/web-form/src/routes/index.test.tsx`
- Delete: `apps/web-form/src/index.tsx`
- Delete: `apps/web-form/src/middleware/logger.ts`

- [ ] **Step 1: 失敗するレンダリングテストを書く**

```tsx
// apps/web-form/src/routes/index.test.tsx
import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import { IndexPage } from "./index";

describe("IndexPage", () => {
	it("ホームの見出しを表示すること", () => {
		const html = renderToStaticMarkup(<IndexPage />);

		expect(html).toContain("フォーム");
		expect(html).toContain("ホーム");
	});
});
```

- [ ] **Step 2: テストが失敗することを確認する**

Run: `pnpm --filter web-form test`

Expected: `Cannot find module '@tanstack/react-router'` または root route 未整備起因の失敗ではなく、`IndexPage` そのものは import できる状態まで持っていく前提で一度失敗を確認する

- [ ] **Step 3: index route を TanStack Start 形式へ更新する**

```tsx
// apps/web-form/src/routes/index.tsx
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
	component: IndexPage,
});

export function IndexPage() {
	return (
		<div className="min-h-screen flex flex-col">
			<header className="bg-white shadow-sm border-b h-16 flex items-center px-6">
				<h1 className="text-xl font-semibold">フォーム</h1>
			</header>

			<main className="flex-1 p-6 max-w-6xl mx-auto w-full">
				<h2 className="text-2xl font-bold mb-6">ホーム</h2>
			</main>

			<footer className="bg-white border-t h-16 flex items-center px-6">
				<p className="text-sm">Footer</p>
			</footer>
		</div>
	);
}
```

- [ ] **Step 4: Spiceflow の旧エントリポイントと middleware を削除する**

Run: `rm apps/web-form/src/index.tsx apps/web-form/src/middleware/logger.ts`

Expected: `apps/web-form` から Spiceflow 固有エントリポイントが消える

- [ ] **Step 5: web-form のテストを再実行する**

Run: `pnpm --filter web-form test`

Expected: `1 passed`

- [ ] **Step 6: web-form の build と typecheck を確認する**

Run: `pnpm --filter web-form typecheck && pnpm --filter web-form build`

Expected: どちらも成功し、TanStack Start のビルドが通る

- [ ] **Step 7: コミット**

```bash
git add apps/web-form/src/routes/index.tsx apps/web-form/src/routes/index.test.tsx apps/web-form/src/index.tsx apps/web-form/src/middleware/logger.ts
git commit -m "feat(web-form): migrate home route to TanStack Start"
```

---

## Task 3: web-tool の TanStack Start 土台と検索クエリ処理を移植する

**Files:**
- Modify: `apps/web-tool/package.json`
- Modify: `apps/web-tool/vite.config.ts`
- Modify: `apps/web-tool/tsconfig.json`
- Create: `apps/web-tool/src/router.tsx`
- Create: `apps/web-tool/src/routes/__root.tsx`
- Create: `apps/web-tool/src/lib/search-params.ts`
- Create: `apps/web-tool/src/lib/search-params.test.ts`
- Modify: `apps/web-tool/src/routes/index.tsx`
- Delete: `apps/web-tool/src/index.tsx`
- Delete: `apps/web-tool/src/middleware/logger.ts`
- Delete: `apps/web-tool/src/routes/error.ts`

- [ ] **Step 1: web-tool の package.json を TanStack Start 構成へ更新する**

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
    "test": "wireit",
    "coverage": "wireit",
    "generate:openapi": "wireit"
  },
  "wireit": {
    "generate:openapi": {
      "command": "openapi-ts"
    },
    "dev": {
      "command": "vite dev --port 4000",
      "service": true,
      "dependencies": [
        "generate:openapi"
      ]
    },
    "build": {
      "command": "vite build",
      "dependencies": [
        "generate:openapi"
      ]
    },
    "typecheck": {
      "command": "tsc --noEmit",
      "dependencies": [
        "generate:openapi"
      ]
    },
    "test": {
      "command": "vitest --run",
      "dependencies": [
        "generate:openapi"
      ]
    },
    "coverage": {
      "command": "vitest --run --coverage",
      "dependencies": [
        "generate:openapi"
      ]
    }
  },
  "dependencies": {
    "@tanstack/react-router": "catalog:",
    "@tanstack/react-start": "catalog:",
    "react": "catalog:",
    "react-dom": "catalog:",
    "pino": "catalog:",
    "tailwindcss": "catalog:",
    "temporal-polyfill-lite": "catalog:",
    "zod": "catalog:"
  },
  "devDependencies": {
    "@hey-api/openapi-ts": "catalog:",
    "@tailwindcss/vite": "catalog:",
    "@tanstack/router-plugin": "catalog:",
    "@types/react": "catalog:",
    "@types/react-dom": "catalog:",
    "@vitejs/plugin-react": "catalog:",
    "@vitest/coverage-v8": "catalog:",
    "vitest": "catalog:",
    "vite": "catalog:"
  }
}
```

- [ ] **Step 2: web-tool の Vite / TypeScript / router / root route を追加する**

```ts
// apps/web-tool/vite.config.ts
import tailwindcss from "@tailwindcss/vite";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
	clearScreen: false,
	server: { port: 4000 },
	plugins: [tanstackStart(), react(), tailwindcss()],
});
```

```json
// apps/web-tool/tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noEmit": true,
    "incremental": true,
    "skipLibCheck": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "resolveJsonModule": true,
    "noUncheckedIndexedAccess": false,
    "forceConsistentCasingInFileNames": true,
    "jsx": "react-jsx",
    "module": "esnext",
    "moduleResolution": "bundler",
    "target": "ES2024",
    "lib": ["dom", "dom.iterable", "esnext"]
  },
  "include": ["src/**/*.ts", "src/**/*.tsx"],
  "exclude": ["node_modules", "dist", ".tanstack", ".output"]
}
```

```ts
// apps/web-tool/src/router.tsx
import { createRouter } from "@tanstack/react-router";
import { routeTree } from "./routeTree.gen";

export function getRouter() {
	return createRouter({
		routeTree,
		scrollRestoration: true,
	});
}

declare module "@tanstack/react-router" {
	interface Register {
		router: ReturnType<typeof getRouter>;
	}
}
```

```tsx
// apps/web-tool/src/routes/__root.tsx
/// <reference types="vite/client" />
import { HeadContent, Outlet, Scripts, createRootRoute } from "@tanstack/react-router";
import appCss from "../style.css?url";

export const Route = createRootRoute({
	head: () => ({
		meta: [
			{ charSet: "utf-8" },
			{ name: "robots", content: "noindex" },
			{ name: "viewport", content: "width=device-width, initial-scale=1.0, viewport-fit=cover" },
			{ title: "Web Tool" },
		],
		links: [{ rel: "stylesheet", href: appCss }],
	}),
	component: RootComponent,
});

function RootComponent() {
	return (
		<html lang="ja">
			<head>
				<HeadContent />
			</head>
			<body className="min-h-screen bg-gray-50">
				<Outlet />
				<Scripts />
			</body>
		</html>
	);
}
```

- [ ] **Step 3: 失敗する検索パラメータテストを書く**

```ts
// apps/web-tool/src/lib/search-params.test.ts
import { describe, expect, it } from "vitest";
import { parseSearchParams, toDownloadSearchParams } from "./search-params";

describe("search params", () => {
	it("デフォルト値を補完してダウンロード用クエリへ変換できること", () => {
		const query = parseSearchParams({ isbn: "978", limit: "10" });
		const search = toDownloadSearchParams(query).toString();

		expect(query.order).toBe("-published_at");
		expect(query.offset).toBe(0);
		expect(query.limit).toBe(10);
		expect(search).toContain("isbn=978");
		expect(search).toContain("limit=10");
	});
});
```

- [ ] **Step 4: 検索パラメータ helper を実装する**

```ts
// apps/web-tool/src/lib/search-params.ts
import { z } from "zod";

export const searchParamSchema = z.object({
	isbn: z.string().optional(),
	title: z.string().optional(),
	status: z.string().optional(),
	priceFrom: z.coerce.number().int().optional(),
	priceTo: z.coerce.number().int().optional(),
	publishedAtStart: z.iso.datetime({ local: true, offset: true }).optional(),
	publishedAtEnd: z.iso.datetime({ local: true, offset: true }).optional(),
	order: z.string().default("-published_at"),
	offset: z.coerce.number().int().default(0),
	limit: z.coerce.number().int().default(20),
});

export type SearchParams = z.infer<typeof searchParamSchema>;

export function parseSearchParams(search: Record<string, unknown>): SearchParams {
	return searchParamSchema.parse(search);
}

export function toDownloadSearchParams(query: SearchParams) {
	return new URLSearchParams(
		Object.entries(query)
			.filter(([, value]) => value !== undefined && value !== null)
			.map(([key, value]) => [key, String(value)]),
	);
}
```

- [ ] **Step 5: web-tool の index route を TanStack Start 形式へ移植する**

```tsx
// apps/web-tool/src/routes/index.tsx
import { createFileRoute } from "@tanstack/react-router";
import { parseSearchParams, toDownloadSearchParams } from "../lib/search-params";

export const Route = createFileRoute("/")({
	validateSearch: (search) => parseSearchParams(search),
	component: IndexRoute,
});

function IndexRoute() {
	const query = Route.useSearch();
	return <IndexPage query={query} />;
}

type Props = {
	query: ReturnType<typeof Route.useSearch>;
};

export function IndexPage({ query }: Props) {
	const downloadUrl = `/books/download?${toDownloadSearchParams(query)}`;

	return (
		<div className="min-h-screen flex flex-col">
			<header className="bg-white shadow-sm border-b h-16 flex items-center px-6">
				<h1 className="text-xl font-semibold text-gray-900">Web Tool</h1>
			</header>

			<main className="flex-1 p-6 max-w-6xl mx-auto w-full">
				<h2 className="text-2xl font-bold text-gray-900 mb-6">検索パラメータ</h2>

				<div className="bg-white rounded-lg shadow p-6 mb-6">
					<h3 className="text-lg font-semibold text-gray-700 mb-4">パース済みクエリ</h3>
					<pre className="bg-gray-100 rounded p-4 overflow-auto text-sm">{JSON.stringify(query, null, 2)}</pre>
				</div>

				<div className="bg-white rounded-lg shadow p-6">
					<a
						href={downloadUrl}
						className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
					>
						<svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-label="Download">
							<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
						</svg>
						ダウンロード
					</a>
				</div>
			</main>

			<footer className="bg-white border-t h-16 flex items-center px-6">
				<p className="text-sm text-gray-600">Footer</p>
			</footer>
		</div>
	);
}
```

- [ ] **Step 6: 旧 Spiceflow エントリポイント群を削除する**

Run: `rm apps/web-tool/src/index.tsx apps/web-tool/src/middleware/logger.ts apps/web-tool/src/routes/error.ts`

Expected: `apps/web-tool` から Spiceflow 依存の入口と error handler が消える

- [ ] **Step 7: web-tool の検索パラメータテストと typecheck を実行する**

Run: `pnpm --filter web-tool test && pnpm --filter web-tool typecheck`

Expected: `search-params.test.ts` が通り、route tree も生成される

- [ ] **Step 8: コミット**

```bash
git add apps/web-tool/package.json apps/web-tool/vite.config.ts apps/web-tool/tsconfig.json apps/web-tool/src/router.tsx apps/web-tool/src/routes/__root.tsx apps/web-tool/src/lib/search-params.ts apps/web-tool/src/lib/search-params.test.ts apps/web-tool/src/routes/index.tsx apps/web-tool/src/routeTree.gen.ts apps/web-tool/src/index.tsx apps/web-tool/src/middleware/logger.ts apps/web-tool/src/routes/error.ts
git commit -m "feat(web-tool): migrate search page to TanStack Start"
```

---

## Task 4: `web-tool` の `/books/download` server route とプロキシテストを追加する

**Files:**
- Create: `apps/web-tool/src/lib/download-proxy.ts`
- Create: `apps/web-tool/src/lib/download-proxy.test.ts`
- Create: `apps/web-tool/src/routes/books/download.tsx`
- Delete: `apps/web-tool/src/routes/books/download.ts`

- [ ] **Step 1: 失敗するプロキシテストを書く**

```ts
// apps/web-tool/src/lib/download-proxy.test.ts
import { describe, expect, it } from "vitest";
import { createProxyRequest } from "./download-proxy";

describe("createProxyRequest", () => {
	it("パスとクエリを維持した upstream request を作ること", () => {
		const request = new Request("http://localhost:4000/books/download?isbn=978&limit=10", {
			method: "GET",
			headers: { accept: "text/csv" },
		});

		const proxied = createProxyRequest(request, "http://localhost:8080");

		expect(proxied.url).toBe("http://localhost:8080/books/download?isbn=978&limit=10");
		expect(proxied.method).toBe("GET");
		expect(proxied.headers.get("accept")).toBe("text/csv");
	});
});
```

- [ ] **Step 2: プロキシ helper を実装する**

```ts
// apps/web-tool/src/lib/download-proxy.ts
const DEFAULT_TARGET_ENDPOINT = "http://localhost:8080";

export function getTargetEndpoint() {
	return process.env.BOOKS_API_BASE_URL ?? DEFAULT_TARGET_ENDPOINT;
}

export function createProxyRequest(request: Request, targetEndpoint = getTargetEndpoint()) {
	const url = new URL(request.url);
	return new Request(new URL(url.pathname + url.search, targetEndpoint), request);
}

export async function proxyDownload(request: Request, fetchImpl: typeof fetch = fetch) {
	return await fetchImpl(createProxyRequest(request));
}
```

- [ ] **Step 3: TanStack Start の server route を作る**

```tsx
// apps/web-tool/src/routes/books/download.tsx
import { createFileRoute } from "@tanstack/react-router";
import { proxyDownload } from "../../lib/download-proxy";

export const Route = createFileRoute("/books/download")({
	server: {
		handlers: {
			GET: async ({ request }) => {
				try {
					return await proxyDownload(request);
				} catch (error) {
					console.error("books_download_proxy_failed", error);
					return new Response("Upstream request failed", { status: 502 });
				}
			},
		},
	},
});
```

- [ ] **Step 4: 旧 download handler を削除する**

Run: `rm apps/web-tool/src/routes/books/download.ts`

Expected: Spiceflow 固有の `SpiceflowRequest` 型依存が消える

- [ ] **Step 5: web-tool のテストを再実行する**

Run: `pnpm --filter web-tool test`

Expected: `2 passed`

- [ ] **Step 6: web-tool の build を確認する**

Run: `pnpm --filter web-tool build`

Expected: `/` と `/books/download` を含む TanStack Start ビルドが成功する

- [ ] **Step 7: コミット**

```bash
git add apps/web-tool/src/lib/download-proxy.ts apps/web-tool/src/lib/download-proxy.test.ts apps/web-tool/src/routes/books/download.tsx apps/web-tool/src/routes/books/download.ts
git commit -m "feat(web-tool): migrate download route to TanStack Start"
```

---

## Task 5: ルート全体の検証と最終クリーンアップを行う

**Files:**
- Modify: 必要なら `apps/web-form/src/components/layout.tsx`
- Modify: 必要なら `apps/web-tool/src/components/layout.tsx`
- Modify: 必要なら `apps/web-form/src/logger.ts`
- Modify: 必要なら `apps/web-tool/src/logger.ts`

- [ ] **Step 1: 両アプリの個別テストを確認する**

Run: `pnpm --filter web-form test && pnpm --filter web-tool test`

Expected: 両方成功する

- [ ] **Step 2: 両アプリの個別 build / typecheck を確認する**

Run: `pnpm --filter web-form typecheck && pnpm --filter web-form build && pnpm --filter web-tool typecheck && pnpm --filter web-tool build`

Expected: すべて成功する

- [ ] **Step 3: ワークスペース全体のフロントエンド検証を行う**

Run: `pnpm test && pnpm build && pnpm check`

Expected: すべて成功する

- [ ] **Step 4: 目視確認を行う**

Run: `pnpm dev`

Expected:
- `http://localhost:3000/` で `フォーム` / `ホーム` が表示される
- `http://localhost:4000/?isbn=978&limit=10` で `検索パラメータ` とダウンロードボタンが表示される
- `http://localhost:4000/books/download?isbn=978&limit=10` が backend へ転送される

- [ ] **Step 5: layout 関数ファイルが未使用なら削除する**

Run: `git grep -n "components/layout" apps/web-form apps/web-tool`

Expected: 未参照なら `apps/web-form/src/components/layout.tsx` と `apps/web-tool/src/components/layout.tsx` を削除対象にできる。参照が残るなら残置する

- [ ] **Step 6: 最終コミット**

```bash
git add pnpm-workspace.yaml apps/web-form apps/web-tool
git commit -m "feat: migrate web apps from spiceflow to TanStack Start"
```

---

## セルフレビュー結果

### Spec coverage

- `web-form` の `/` 維持: Task 1-2 で対応
- `web-tool` の `/` 維持: Task 3 で対応
- `web-tool` の `/books/download` 維持: Task 4 で対応
- build / typecheck / test 成立: Task 5 で対応
- `spiceflow` 依存除去: Task 1, 3, 4 で対応

### Placeholder scan

- プレースホルダ語や実装先送り表現は未使用
- 生成ファイル `routeTree.gen.ts` は手書き対象ではなく、生成確認ステップで扱っている

### Type consistency

- 検索クエリの型は `SearchParams` に集約
- ダウンロード URL 生成は `toDownloadSearchParams()` に集約
- プロキシ処理は `createProxyRequest()` / `proxyDownload()` で一貫させている
