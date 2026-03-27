---
name: wireit-monorepo
description: npm/pnpmベースのモノレポでWireitを使用してビルドパイプラインの依存関係を管理し、キャッシュと並列実行を活用する場合に使用する。
license: MIT
compatibility: Node.js 18+, pnpm/npm, Wireit 0.14+
metadata:
  author: ymkz
  version: "1.0"
---

# Wireit Monorepo

## Overview

Wireitを使用して、npm/pnpmベースのモノレポでビルドスクリプトの依存関係を宣言的に管理し、キャッシュと並列実行によりビルドを高速化する。

## When to Use

- モノレポ内の複数パッケージでビルド順序を管理したい場合
- スクリプトの重複実行を防ぎたい場合
- ファイル変更に基づくインクリメンタルビルドを実現したい場合
- ローカルとCIで一貫したビルドパイプラインを維持したい場合
- `build` → `test` → `deploy` のような依存チェーンを管理したい場合

## Configuration

### Root package.json

```json
{
  "name": "my-monorepo",
  "packageManager": "pnpm@10.32.1",
  "scripts": {
    "dev": "wireit",
    "build": "wireit",
    "typecheck": "wireit",
    "test": "wireit",
    "coverage": "wireit",
    "check": "wireit",
    "lint": "biome check",
    "format": "biome check --write"
  },
  "wireit": {
    "dev": {
      "dependencies": [
        "./apps/web-form:dev",
        "./apps/web-tool:dev"
      ]
    },
    "build": {
      "dependencies": [
        "./apps/web-form:build",
        "./apps/web-tool:build"
      ]
    },
    "typecheck": {
      "dependencies": [
        "./apps/web-form:typecheck",
        "./apps/web-tool:typecheck"
      ]
    },
    "test": {
      "dependencies": [
        "./apps/web-form:test",
        "./apps/web-tool:test"
      ]
    },
    "coverage": {
      "dependencies": [
        "./apps/web-form:coverage",
        "./apps/web-tool:coverage"
      ]
    },
    "check": {
      "dependencies": [
        "lint",
        "typecheck",
        "build"
      ]
    }
  },
  "devDependencies": {
    "wireit": "catalog:"
  }
}
```

### App package.json

```json
{
  "name": "web-form",
  "scripts": {
    "dev": "wireit",
    "build": "wireit",
    "typecheck": "wireit",
    "test": "wireit",
    "generate:openapi": "wireit"
  },
  "wireit": {
    "generate:openapi": {
      "command": "openapi-ts"
    },
    "dev": {
      "command": "next dev --turbopack -p 3000",
      "service": true,
      "dependencies": ["generate:openapi"]
    },
    "build": {
      "command": "next build",
      "dependencies": ["generate:openapi"]
    },
    "typecheck": {
      "command": "tsc --noEmit",
      "dependencies": ["generate:openapi"]
    },
    "test": {
      "command": "vitest --run",
      "dependencies": ["generate:openapi"]
    }
  }
}
```

## Key Concepts

| プロパティ | 説明 |
|-----------|------|
| `command` | 実行するシェルコマンド |
| `dependencies` | 実行前に完了する必要があるスクリプト |
| `service` | 起動後も継続して実行されるプロセス（dev server等） |
| `files` | キャッシュキーに含める入力ファイル |
| `output` | キャッシュ対象の出力ファイル/ディレクトリ |

## Dependency Types

### Cross-package dependencies

```json
{
  "wireit": {
    "build": {
      "dependencies": [
        "../shared:build"
      ]
    }
  }
}
```

### Same-package dependencies

```json
{
  "wireit": {
    "build": {
      "dependencies": ["generate:types"]
    },
    "test": {
      "dependencies": ["build"]
    }
  }
}
```

### Service dependencies

```json
{
  "wireit": {
    "test:e2e": {
      "command": "playwright test",
      "dependencies": ["dev:server"]
    },
    "dev:server": {
      "command": "next dev",
      "service": true
    }
  }
}
```

## Caching Configuration

### 入力・出力の明示

```json
{
  "wireit": {
    "build": {
      "command": "tsc",
      "files": [
        "src/**/*.ts",
        "tsconfig.json"
      ],
      "output": [
        "dist/**"
      ]
    },
    "test": {
      "command": "vitest --run",
      "files": [
        "src/**/*.ts",
        "tests/**/*.ts"
      ],
      "dependencies": ["build"]
    }
  }
}
```

### GitHub Actionsでのキャッシュ活用

```yaml
# .github/workflows/ci.yml
- uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'pnpm'

- uses: google/wireit@setup-github-actions-caching/v2

- run: pnpm check
```

## Common Patterns

### OpenAPI-driven workflow

```json
{
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
    },
    "build": {
      "command": "next build",
      "dependencies": ["generate:openapi"]
    }
  }
}
```

### Type-check → Test → Coverage chain

```json
{
  "wireit": {
    "typecheck": {
      "command": "tsc --noEmit",
      "files": ["src/**/*.ts", "tsconfig.json"]
    },
    "test": {
      "command": "vitest --run",
      "dependencies": ["typecheck"],
      "files": ["src/**/*.ts", "tests/**/*.ts"]
    },
    "coverage": {
      "command": "vitest --run --coverage",
      "dependencies": ["test"]
    }
  }
}
```

### Parallel execution

```json
{
  "wireit": {
    "check": {
      "dependencies": [
        "lint",
        "typecheck",
        "test"
      ]
    }
  }
}
```

上記の設定により、`lint`, `typecheck`, `test` は並列に実行される。

## Best Practices

### ✅ 推奨パターン

```json
{
  "wireit": {
    "build": {
      "command": "tsc",
      "files": ["src/**"],
      "output": ["dist/**"],
      "clean": "if-file-deleted"
    }
  }
}
```

### ❌ 避けるべきパターン

```json
{
  "wireit": {
    "build": {
      "command": "rm -rf dist && tsc",  // 不要: cleanがある
      "dependencies": ["build"]          // 循環依存！
    }
  }
}
```

### Service scriptの適切な使用

```json
{
  "wireit": {
    "dev": {
      "command": "next dev",
      "service": true,
      "dependencies": ["generate:types"]
    }
  }
}
```

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| 無限ループ | 循環依存 | 依存グラフを見直す |
| キャッシュヒットしない | files/output設定不足 | 入力出力ファイルを明示 |
| Serviceが停止しない | シグナル処理 | graceful shutdown実装 |
| 並列実行でエラー | リソース競合 | dependenciesで順序制御 |

## Comparison: npm vs Wireit

### Before: npm run-all

```json
{
  "scripts": {
    "check": "npm run lint && npm run typecheck && npm run test",
    "dev": "npm-run-all --parallel web-form:dev web-tool:dev"
  }
}
```

問題:
- キャッシュなし
- 依存関係の重複実行
- 複雑な並列制御

### After: Wireit

```json
{
  "scripts": {
    "check": "wireit",
    "dev": "wireit"
  },
  "wireit": {
    "check": {
      "dependencies": ["lint", "typecheck", "test"]
    },
    "dev": {
      "dependencies": [
        "./apps/web-form:dev",
        "./apps/web-tool:dev"
      ]
    }
  }
}
```

利点:
- 自動キャッシュ
- 重複実行防止
- 宣言的な依存関係

## References

- [Wireit公式ドキュメント](https://github.com/google/wireit)
- [Wireit GitHub Actionsキャッシュ](https://github.com/google/wireit#github-actions-caching)
