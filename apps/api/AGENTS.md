# API APPLICATION

## OVERVIEW
Spring Boot REST API with MyBatis persistence and OpenAPI-first design

## STRUCTURE

```
src/main/java/dev/ymkz/demo/api/
├── presentation/      # Controllers and DTOs
│   ├── controller/
│   └── dto/
├── application/       # Use cases
│   └── usecase/
├── domain/            # Domain models and repositories
│   ├── model/
│   └── repository/
└── infrastructure/    # MyBatis mappers and external APIs
    ├── datasource/
    └── externalapi/
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Controllers | `presentation/controller/` | Spring MVC annotated |
| DTOs | `presentation/dto/` | Request/response objects |
| Use Cases | `application/usecase/` | Business logic layer |
| Domain Models | `domain/model/` | Entities |
| Domain Repositories | `domain/repository/` | Repository interfaces |
| MyBatis Mappers | `infrastructure/datasource/` | SQL mapping files |
| External API Clients | `infrastructure/externalapi/` | JSONPlaceholder integration |
| Integration Tests | `src/intTest/` | TestContainers + MySQL |
| OpenAPI Config | `build.gradle.kts` | `springdoc-openapi` plugin |

## CONVENTIONS

### Build & Testing
- **Springdoc OpenAPI plugin**: Generates `openapi.json` at build time
- **MyBatis mappers**: XML files in `infrastructure/datasource/mapper`
- **Custom source set**: `src/intTest` for integration tests
- **TestContainers**: MySQL container spun up for integration tests

### Code Quality
- **Spotless**: Palantir Java Format
- **Lombok**: Annotation processor for boilerplate reduction
- **Kotlin**: Full Kotlin implementation
- **Japanese test names**: Use Japanese for test cases

### Dependency Strategy
- **Gradle version catalog**: Centralized `gradle/libs.versions.toml`
- **Core module dependency**: `project(":apps:core")` for shared domain
- **Spring Boot BOM**: Managed dependency versions

## POST-MODIFICATION VALIDATION

コード修正後は必ず以下を実行し、すべてパスすることを確認する：

```bash
./gradlew :apps:api:test           # 単体テスト
./gradlew :apps:api:intTest        # 統合テスト
./gradlew :apps:api:spotlessCheck  # フォーマットチェック
./gradlew :apps:api:build          # ビルド（テスト含む）
```

失敗した場合：
1. エラーを修正
2. `./gradlew :apps:api:spotlessApply` で自動修正可能な場合は適用
3. 再度実行してパス確認
4. すべてパスしてからコミット

## ANTI-PATTERNS

| Pattern | Why Forbidden |
|---------|---------------|
| Hardcoded secrets in config | Security risk |
| Missing database migrations | Schema drift |
| DTO directly exposed | Violates domain model encapsulation |
| Business logic in controllers | Violates Clean Architecture |
