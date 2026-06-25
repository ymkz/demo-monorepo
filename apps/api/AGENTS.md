# API APPLICATION

## OVERVIEW

Spring Boot REST API with MyBatis persistence and OpenAPI-first design.

## STRUCTURE

```text
src/main/java/dev/ymkz/demo/api/
├── config/             # Spring/MyBatis configuration
│   └── mybatis/
├── external/           # External API clients
│   └── jsonplaceholder/
├── features/           # Feature-first packages
│   └── books/          # Book feature classes are mostly flat
│       └── dto/        # Request/response/query DTOs
└── shared/             # Cross-cutting concerns
    ├── exception/
    └── logging/
```

`features/books` は、Controller / Usecase / domain model / repository interface / datasource / mapper を直下に置く。
DTOのみ数が増えやすいため `features/books/dto` に分ける。

## WHERE TO LOOK

| Task                 | Location                    | Notes                                   |
| -------------------- | --------------------------- | --------------------------------------- |
| Book feature         | `features/books/`           | Main classes are flat under the feature |
| Book DTOs            | `features/books/dto/`       | Request/response/query objects          |
| Shared exceptions    | `shared/exception/`         | Global exception handling               |
| Wide event logging   | `shared/logging/`           | Request-scoped wide event logging       |
| MyBatis config       | `config/mybatis/`           | MyBatis configuration and interceptors  |
| External API clients | `external/jsonplaceholder/` | JSONPlaceholder integration             |
| Unit tests           | `src/test/`                 | Mirrors feature package where practical |
| Integration tests    | `src/intTest/`              | TestContainers + MySQL                  |
| OpenAPI Config       | `build.gradle.kts`          | `springdoc-openapi` plugin              |

## CONVENTIONS

### Naming

- Use `Usecase`, not `UseCase`.
- Keep feature classes under `features/<feature>/` unless the feature grows enough to justify subpackages.
- Keep DTOs under `features/<feature>/dto/`.

### Build & Testing

- **Springdoc OpenAPI plugin**: Generates `openapi.json` at build time
- **MyBatis mappers**: Mapper interfaces live with the feature package
- **Custom source set**: `src/intTest` for integration tests
- **TestContainers**: MySQL container spun up for integration tests

### Code Quality

- **Spotless**: Palantir Java Format
- **Lombok**: Annotation processor for boilerplate reduction
- **Java**: Main implementation language for the API app
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

| Pattern                       | Why Forbidden                       |
| ----------------------------- | ----------------------------------- |
| Hardcoded secrets in config   | Security risk                       |
| Missing database migrations   | Schema drift                        |
| DTO directly exposed          | Violates domain model encapsulation |
| Business logic in controllers | Violates Clean Architecture         |
