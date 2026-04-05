# WEB-TOOL APP

**Generated:** 2026-03-29
**Parent:** /AGENTS.md

## OVERVIEW
Spiceflow RSC (React Server Components) application with Tailwind CSS.

## STRUCTURE
```
apps/web-tool/
├── app/                  # Spiceflow application
│   ├── main.tsx         # App entry with routes
│   └── pages/           # RSC pages
│       └── index.tsx    # Search page
├── src/
│   └── generated/       # Auto-generated from OpenAPI
│       ├── client/      # API client
│       └── core/        # Core types
├── vite.config.ts       # Vite + Spiceflow plugin
└── package.json
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Routes | `app/main.tsx` | Spiceflow routes with type-safe handlers |
| Pages | `app/pages/` | RSC components |
| API Client | `src/generated/client/` | Auto-generated from backend OpenAPI |
| Config | `openapi-ts.config.ts` | OpenAPI codegen configuration |
| Styles | Tailwind CSS | Utility-first styling |

## CONVENTIONS

### Spiceflow Patterns
- Routes defined in `main.tsx` using `.route()` method
- RSC pages in `pages/` directory
- Layout at root level with `.layout('/*', ...)`
- Type-safe queries using Zod schemas

### API Routes
- Use `.route()` with method/path/query/response schemas
- Proxy to backend via fetch()
- Environment variables for tokens (API_TOKEN)

### Development
- Port 4000 (via `vite dev --port 4000`)
- Vite for fast HMR
- Biome for linting (run `pnpm lint`)

## ANTI-PATTERNS

| Pattern | Why Forbidden |
|---------|---------------|
| Hardcoded secrets | Use environment variables |
| Manual API types | Use generated OpenAPI types instead |
| Client-side state | Use RSC server state instead |
