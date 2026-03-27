---
name: openapi-first
description: OpenAPI仕様を駆動としてバックエンドとフロントエンドの型安全性を確保する場合に使用する。バックエンドでOpenAPIを生成し、フロントエンドでコード生成して型共有を実現する。
license: MIT
compatibility: Spring Boot 3.x + Springdoc, Node.js 18+, @hey-api/openapi-ts
metadata:
  author: ymkz
  version: "1.0"
---

# OpenAPI First Development

## Overview

バックエンド（Spring Boot）でOpenAPI仕様を生成し、フロントエンド（TypeScript）でコード生成することで、API契約に基づく型安全性を確保する開発手法。

## When to Use

- バックエンドとフロントエンド間で型の不一致が発生する場合
- API仕様をシングルソースオブトゥルースとして管理したい場合
- 手書きのAPI型定義のメンテナンスコストを削減したい場合
- マイクロサービス間のAPI定義を標準化したい場合

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        OpenAPI First                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Backend (Spring Boot)                                      │
│  ┌────────────────────┐      ┌──────────────────┐          │
│  │ @RestController    │──────│ springdoc-openapi │          │
│  │ @Schema            │      │ (OpenAPI生成)     │          │
│  └────────────────────┘      └────────┬─────────┘          │
│                                        │ openapi.json       │
│  ┌─────────────────────────────────────┴─────────────────┐  │
│  │              src/main/resources/static/openapi/        │  │
│  │                   openapi.json (Git管理)               │  │
│  └────────────────────────────────────────────────────────┘  │
│                              │                              │
└──────────────────────────────┼──────────────────────────────┘
                               │
Frontend (Next.js)             │
│                              │
│  ┌───────────────────────────┴──────────────────────────┐   │
│  │ @hey-api/openapi-ts                                    │   │
│  │  ├── client/ (APIクライアント)                         │   │
│  │  ├── types/ (TypeScript型)                             │   │
│  │  └── core/ (共通型)                                    │   │
│  └────────────────────────────────────────────────────────┘   │
│                              │                                │
│  ┌───────────────────────────┘                                │
│  │ fetch('/api/books')                                        │
│  │   ↓ 型安全なAPI呼び出し                                     │
│  └────────────────────────────────────────────────────────────┘
```

## Backend Configuration

### Spring Boot + Springdoc

`apps/api/build.gradle.kts`:

```kotlin
plugins {
    id("common-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.springdoc.openapi)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.springdoc.openapi.starter.webmvc.api)
    // ...
}

openApi {
    apiDocsUrl.set("http://localhost:8080/openapi/openapi.json")
    outputDir.set(rootProject.file("apps/api/src/main/resources/static/openapi"))
    outputFileName.set("openapi.json")
}

tasks.named("build") {
    dependsOn("generateOpenApiDocs")
}
```

### Controller with OpenAPI Annotations

```java
@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "書籍管理API")
public class BookController {

    @GetMapping
    @Operation(summary = "書籍一覧取得")
    public ResponseEntity<BookSearchResponse> search(
            @Parameter(description = "検索キーワード")
            @RequestParam(required = false) String keyword) {
        // ...
    }
}

@Schema(description = "書籍検索レスポンス")
public record BookSearchResponse(
    @Schema(description = "書籍リスト")
    List<Book> books,
    
    @Schema(description = "総件数")
    long total
) {}
```

## Frontend Configuration

### @hey-api/openapi-ts

`apps/web-form/package.json`:

```json
{
  "scripts": {
    "generate:openapi": "openapi-ts",
    "dev": "wireit",
    "build": "wireit"
  },
  "wireit": {
    "generate:openapi": {
      "command": "openapi-ts",
      "files": [
        "../../apps/api/src/main/resources/static/openapi/openapi.json"
      ],
      "output": [
        "src/generated/**"
      ]
    },
    "dev": {
      "command": "next dev",
      "service": true,
      "dependencies": ["generate:openapi"]
    }
  },
  "devDependencies": {
    "@hey-api/openapi-ts": "catalog:"
  }
}
```

### openapi-ts.config.ts

```typescript
import { defineConfig } from '@hey-api/openapi-ts';

export default defineConfig({
  client: '@hey-api/client-fetch',
  input: '../../apps/api/src/main/resources/static/openapi/openapi.json',
  output: {
    path: './src/generated',
  },
  types: {
    enums: 'typescript',
  },
});
```

### Generated Code Structure

```
src/generated/
├── client/
│   └── services.gen.ts    # APIクライアント関数
├── types/
│   └── types.gen.ts       # TypeScript型定義
└── core/
    └── ...                # 共通ユーティリティ
```

## Workflow

### 1. Backend Development

```java
// BookController.java
@PostMapping
@Operation(summary = "書籍登録")
public ResponseEntity<BookResponse> create(
        @RequestBody @Valid BookCreateRequest request) {
    // 実装
}
```

### 2. OpenAPI Generation

```bash
# GradleタスクでOpenAPI仕様を生成
./gradlew :apps:api:generateOpenApiDocs

# 出力: apps/api/src/main/resources/static/openapi/openapi.json
```

### 3. Frontend Code Generation

```bash
# pnpmでTypeScriptコードを生成
cd apps/web-form
pnpm generate:openapi

# 出力: src/generated/{client,types}/
```

### 4. Type-safe API Calls

```typescript
// Next.js App Router (Server Component)
import { getBooks } from '@/generated/client';

export default async function BooksPage() {
  // 型安全なAPI呼び出し
  const { data: books } = await getBooks({ query: { keyword: 'Spring' } });
  
  return (
    <ul>
      {books?.map(book => (
        <li key={book.id}>{book.title}</li>
      ))}
    </ul>
  );
}
```

## Git Management

### OpenAPI Spec

```gitignore
# apps/web-form/.gitignore
src/generated/
```

```
# Git管理対象
apps/api/src/main/resources/static/openapi/openapi.json  # ✅ バックエンドで生成

# Git管理外（.gitignore）
apps/web-form/src/generated/  # ❌ フロントエンドで生成
```

### Rationale

- **openapi.json**: バックエンドのビルド成果物だが、フロントエンドとの契約としてGit管理
- **src/generated/**: フロントエンドで生成されるためGit管理不要

## Best Practices

### Backend

```java
// ✅ 良い: 明示的なSchemaアノテーション
@Schema(description = "書籍ID", example = "1")
private Long id;

// ❌ 避ける: 暗黙的な型推定
private Long id;  // descriptionなし
```

```java
// ✅ 良い: バリデーションと併用
@Schema(description = "書籍タイトル")
@NotBlank
@Size(max = 100)
private String title;
```

### Frontend

```typescript
// ✅ 良い: 生成コードの直接使用
import { getBooks, createBook } from '@/generated/client';

// ❌ 避ける: 手動での型定義
interface Book {  // 重複！
  id: number;
  title: string;
}
```

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| 型が生成されない | OpenAPI仕様の更新 | `./gradlew generateOpenApiDocs`を再実行 |
| フロントエンドで型エラー | API変更と同期不足 | バックエンド→フロントエンドの順で生成 |
| 循環参照エラー | スキーマ定義の問題 | `@Schema`アノテーションを見直す |
| パス解決エラー | configのパス設定 | `openapi-ts.config.ts`のinputパスを確認 |

## References

- [Springdoc OpenAPI](https://springdoc.org/)
- [@hey-api/openapi-ts](https://heyapi.dev/)
- [OpenAPI Specification](https://swagger.io/specification/)
