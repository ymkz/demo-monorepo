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
