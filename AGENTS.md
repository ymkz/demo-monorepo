# PROJECT KNOWLEDGE BASE

**Generated:** 2026-02-23T21:16:51+09:00
**Commit:** ced8292
**Branch:** main

## OVERVIEW
Hybrid monorepo: Spring Boot (Kotlin) backend + Next.js (TypeScript) frontends. OpenAPI-first design with MySQL persistence.

## STRUCTURE
```
demo-monorepo/
├── apps/
│   ├── api/          # Spring Boot REST API (MyBatis + MySQL)
│   ├── core/         # Shared domain library (value objects, models)
│   ├── web-form/     # Next.js App Router (port 3000)
│   └── web-tool/     # Next.js Pages Router (port 4000)
├── gradle/
│   └── convention/   # Custom Gradle convention plugins
├── compose/mysql/    # Database init scripts
└── docs/             # Database schema, dev/operation guides
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| REST Controllers | `apps/api/src/main/java/dev/ymkz/demo/api/presentation` | Spring MVC |
| Use Cases | `apps/api/src/main/java/dev/ymkz/demo/api/application` | Application logic |
| Domain Models | `apps/api/src/main/java/dev/ymkz/demo/api/domain/model` | Entities |
| Repositories | `apps/api/src/main/java/dev/ymkz/demo/api/infrastructure/datasource` | MyBatis mappers |
| Value Objects | `apps/core/src/main/java/dev/ymkz/demo/core/domain/valueobject` | Shared domain |
| App Router Pages | `apps/web-form/src/app` | Next.js 13+ |
| Pages Router | `apps/web-tool/src/pages` | Next.js 12 style |
| OpenAPI Spec | `apps/api/src/main/resources/static/openapi/openapi.json` | Auto-generated |
| Generated Types | `apps/web-*/src/generated` | TypeScript from OpenAPI |

## CONVENTIONS

### Build & Orchestration
- **Wireit** (not turborepo) for incremental builds
- Root commands orchestrate via wireit dependencies
- Backend: `./gradlew build` | Frontend: `pnpm build`

### Code Quality
- **Biome** for linting/formatting (NOT ESLint/Prettier)
- Line width: 120
- TypeScript strict mode enabled
- **Spotless** (Palantir Java Format) for Java code

### Dependency Management
- **pnpm catalog mode**: strict (centralized versions)
- **Gradle version catalog**: `gradle/libs.versions.toml`

### Testing
- Java: JUnit 5 + AssertJ + TestContainers (MySQL)
- TypeScript: Vitest (no tests yet)
- Integration tests: `src/intTest` custom source set

### OpenAPI Workflow
1. Backend: Spring Boot generates OpenAPI spec
2. Frontend: `@hey-api/openapi-ts` generates TypeScript clients
3. Build runs: `generateOpenApiDocs` → frontend `generate:*`

## ANTI-PATTERNS (THIS PROJECT)

| Pattern | Why Forbidden |
|---------|---------------|
| Hardcoded secrets | Found: `"X-API-Token": "TODO"` in `apps/web-tool/src/pages/api/books/download.ts` |
| TODO comments in commits | Create issues instead |
| Mixed router patterns | web-form (App Router) vs web-tool (Pages Router) creates inconsistency |
| Build output in src | `src/main/resources/static/openapi/` should be in `build/` |

## UNIQUE STYLES

### Japanese Codebase
- All documentation and responses in **Japanese**
- Test names use Japanese (e.g., `enumValues_期待される全ての値が存在すること`)

### Module Structure
- No `packages/` directory (shared code in `apps/core` instead)
- Core is Gradle module only (not usable by Next.js apps)

### Next.js Version
- **Next.js 16.1.6** (latest, possibly beta)
- **React 19.2.4** (latest)

## COMMANDS

```bash
# Development
pnpm dev                # Start both frontends (3000, 4000)
./gradlew bootRun      # Start Spring Boot API

# Build
pnpm build             # Build frontends
./gradlew build        # Build backend (includes spotlessApply + tests)

# Test
./gradlew test         # Java unit tests
./gradlew intTest      # Java integration tests (TestContainers + MySQL)
pnpm test              # Frontend tests (Vitest)

# Code Quality
pnpm lint              # Biome check
pnpm format            # Biome check --write
pnpm typecheck         # TypeScript type check
pnpm check             # lint + typecheck + build

# OpenAPI
pnpm generate          # Generate TypeScript clients from OpenAPI spec
```

## NOTES

### Database
- MySQL 8 required (via Docker Compose)
- Init scripts: `compose/mysql/init/`
- Connection via TestContainers in tests

### Dev Environment
- Node.js 24+ required (engines enforced)
- Java 17/21 required
- pnpm as package manager

### CI/CD
- GitHub Actions: `.github/workflows/ci.yml`
- GitHub Pages deploys OpenAPI docs (not apps)
- No production deployment configured

### Gotchas
- Wireit orchestration: use `pnpm dev` not individual app commands
- Japanese language required in all responses (see `.github/copilot-instructions.md`)
- OpenAPI spec is build artifact, committed to repo
