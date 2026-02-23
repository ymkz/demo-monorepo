# WEB-TOOL APP

**Generated:** 2026-02-23T21:16:51+09:00
**Parent:** /AGENTS.md

## OVERVIEW
Next.js 16 Pages Router application with API routes and Mantine UI.

## STRUCTURE
```
apps/web-tool/src/
├── pages/                 # Next.js Pages Router (legacy)
│   ├── index.tsx          # Home page
│   ├── _app.tsx           # Custom App component
│   ├── _document.tsx      # Custom HTML document
│   └── api/               # Next.js API routes
│       └── books/
│           └── download.ts # Backend proxy endpoint
└── generated/             # Auto-generated from OpenAPI
    ├── client/            # API client
    └── core/              # Core types
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Pages | `src/pages/` | Pages Router file-based routing |
| API Routes | `src/pages/api/` | Next.js API endpoints (server-side) |
| API Client | `src/generated/client/` | Auto-generated from backend OpenAPI |
| Config | `openapi-ts.config.ts` | OpenAPI codegen configuration |
| UI Components | Mantine | UI library |

## CONVENTIONS

### Pages Router Patterns
- `getServerSideProps` for server-rendered pages
- Custom App via `_app.tsx` for global state/providers
- Custom Document via `_document.tsx` for HTML structure
- API routes in `pages/api/` for backend endpoints

### OpenAPI Workflow
1. Backend generates OpenAPI spec via `springdoc-openapi`
2. Frontend runs `pnpm generate:web-tool` → `@hey-api/openapi-ts`
3. Generated code in `src/generated/` (gitignored)

### Development
- Port 4000 (via `next dev --turbopack -p 4000`)
- Turbopack enabled for faster dev builds
- Biome for linting (run `pnpm lint`)

## ANTI-PATTERNS

| Pattern | Why Forbidden |
|---------|---------------|
| **Hardcoded API tokens** | `pages/api/books/download.ts` has `"X-API-Token": "TODO"` - CRITICAL SECURITY ISSUE |
| Manual API types | Use generated OpenAPI types instead |
| Mixed routers | Don't mix App Router patterns into Pages Router |
| Client-only secrets | Never store tokens in frontend code |
