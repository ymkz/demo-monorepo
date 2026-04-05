# WEB-FORM APP

**Generated:** 2026-03-30
**Parent:** /AGENTS.md

## OVERVIEW
Spiceflow RSC (React Server Components) application with Tailwind CSS.

## STRUCTURE
```
apps/web-form/
├── src/
│   ├── index.tsx          # App entry with routes
│   ├── components/        # Shared components
│   │   └── layout.tsx     # Root layout component
│   ├── routes/            # RSC pages
│   │   └── index.tsx      # Home page
│   ├── style.css          # Global styles
│   └── generated/         # Auto-generated from OpenAPI
│       ├── client/        # API client
│       └── core/          # Core types
├── vite.config.ts         # Vite + Spiceflow plugin
└── package.json
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Routes | `src/index.tsx` | Spiceflow routes with type-safe handlers |
| Pages | `src/routes/` | RSC components |
| Layout | `src/components/layout.tsx` | Shared layout component |
| API Client | `src/generated/client/` | Auto-generated from backend OpenAPI |
| Type definitions | `src/generated/core/` | TypeScript types from OpenAPI spec |
| Styles | `src/style.css` | Global CSS with Tailwind CSS |
| Config | `openapi-ts.config.ts` | OpenAPI codegen configuration |

## CONVENTIONS

### Spiceflow Patterns
- Routes defined in `index.tsx` using `.page()` method
- RSC pages in `routes/` directory
- Layout at root level with `.layout('/*', ...)`
- Type-safe queries using Zod schemas (when needed)

### API Routes
- Use `.route()` with method/path/query/response schemas
- Proxy to backend via fetch()
- Environment variables for configuration

### Development
- Port 3000 (via `vite dev`)
- Vite for fast HMR
- Biome for linting (run `pnpm lint`)

## ANTI-PATTERNS

| Pattern | Why Forbidden |
|---------|---------------|
| Manual API types | Use generated OpenAPI types instead |
| Hardcoded secrets | Use environment variables |
| Client-side state | Use RSC server state instead |
