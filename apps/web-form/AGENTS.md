# WEB-FRAME APP

**Generated:** 2026-02-23T21:16:51+09:00
**Parent:** /AGENTS.md

## OVERVIEW
Next.js 16 App Router application consuming OpenAPI-generated backend types.

## STRUCTURE
```
apps/web-form/src/
├── app/                    # Next.js App Router (new)
│   ├── layout.tsx          # Root layout
│   ├── page.tsx            # Home page
│   └── global.css          # Global styles
└── generated/              # Auto-generated from OpenAPI
    ├── client/             # API client
    └── core/               # Core types
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Pages | `src/app/` | App Router file-based routing |
| API Client | `src/generated/client/` | Auto-generated from backend OpenAPI |
| Type definitions | `src/generated/core/` | TypeScript types from OpenAPI spec |
| Styles | `src/app/global.css` | Global CSS with Tailwind/Mantine |
| Config | `openapi-ts.config.ts` | OpenAPI codegen configuration |

## CONVENTIONS

### OpenAPI Workflow
1. Backend generates OpenAPI spec via `springdoc-openapi`
2. Frontend runs `pnpm generate:web-form` → `@hey-api/openapi-ts`
3. Generated code in `src/generated/` (gitignored)
4. Use generated client for type-safe API calls

### App Router Patterns
- Use Server Components by default
- File-based routing: `app/books/[id]/page.tsx`
- `layout.tsx` for shared UI
- `page.tsx` for route handlers

### Development
- Port 3000 (via `next dev --turbopack -p 3000`)
- Turbopack enabled for faster dev builds
- Biome for linting (run `pnpm lint`)

## ANTI-PATTERNS

| Pattern | Why Forbidden |
|---------|---------------|
| Manual API types | Use generated OpenAPI types instead |
| Mixed Server/Client | Use `'use client'` directive explicitly when needed |
| Fetch directly | Use generated API client for consistency |
