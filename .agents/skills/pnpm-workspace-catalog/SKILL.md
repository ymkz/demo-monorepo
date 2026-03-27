---
name: pnpm-workspace-catalog
description: pnpmワークスペースでcatalog機能を使用して複数パッケージの依存バージョンを一元管理する場合に使用する。strictモードによる厳格なバージョン管理を実現する。
license: MIT
compatibility: pnpm 9.5+, Node.js 18+
metadata:
  author: ymkz
  version: "1.0"
---

# pnpm Workspace Catalog

## Overview

pnpmの`catalog:`プロトコルを使用して、ワークスペース全体の依存関係バージョンを一元管理する。

## When to Use

- モノレポ内の複数パッケージで共通の依存関係を使用する場合
- バージョンの不一致による「依存関係の地獄」を防ぎたい場合
- バージョンアップを一箇所で管理したい場合
- catalogMode: strictで厳格な管理を行いたい場合

## Configuration

### pnpm-workspace.yaml

```yaml
packages:
  - apps/*
  - packages/*

# カタログ定義: 全パッケージで共通使用するバージョン
catalog:
  "@types/node": 20.11.0
  "typescript": 5.3.3
  "vitest": 1.2.0
  "react": 18.2.0
  "react-dom": 18.2.0
  "next": 14.1.0

# strictモード: カタログに定義されたパッケージのみ使用可能
catalogMode: strict

# ワークスペースパッケージの自動インジェクション
injectWorkspacePackages: true

# パッケージマネージャーバージョンの自動管理
managePackageManagerVersions: true

# ビルド後の依存関係同期
syncInjectedDepsAfterScripts:
  - build

# 使用するNode.jsバージョン
useNodeVersion: 20.11.0
```

### package.json での使用方法

```json
{
  "name": "my-app",
  "devDependencies": {
    "@types/node": "catalog:",
    "typescript": "catalog:",
    "vitest": "catalog:"
  },
  "dependencies": {
    "react": "catalog:",
    "react-dom": "catalog:"
  }
}
```

## Key Features

| フィールド | 説明 |
|-----------|------|
| `catalog` | パッケージ名とバージョンのマッピング |
| `catalogMode: strict` | カタログ外のバージョン指定を禁止 |
| `injectWorkspacePackages` | ローカルパッケージを自動的にリンク |
| `managePackageManagerVersions` | packageManagerフィールドを自動更新 |
| `useNodeVersion` | 指定Node.jsバージョンの強制使用 |

## Workflow

### 1. 初期セットアップ

```bash
# pnpm-workspace.yamlにcatalogを定義
cat > pnpm-workspace.yaml << 'EOF'
packages:
  - apps/*

catalog:
  "react": 18.2.0
  "typescript": 5.3.3

catalogMode: strict
EOF

# 各パッケージのpackage.jsonでcatalog:を使用
```

### 2. パッケージのインストール

```bash
# catalogバージョンでインストール
pnpm add react@catalog:

# または package.json を直接編集して pnpm install
```

### 3. バージョンアップデート

```bash
# 対話式で最新バージョンに更新
pnpm update --latest --interactive --recursive

# または pnpm-workspace.yaml を手動で編集後
pnpm install
```

## strict モードの挙動

### ✅ 許可されるパターン

```json
{
  "dependencies": {
    "react": "catalog:",           // OK: カタログ参照
    "lodash": "4.17.21"            // OK: カタログにないパッケージ
  }
}
```

### ❌ エラーになるパターン

```json
{
  "dependencies": {
    "react": "18.1.0"              // NG: catalogに定義されたパッケージの別バージョン指定
  }
}
```

エラーメッセージ:
```
ERR_PNPM_CATALOG_ENTRY_INVALID_SPEC: The spec of react (18.1.0) does not match the catalog (18.2.0)
```

## Best Practices

### カタログに含めるべきパッケージ

```yaml
catalog:
  # フレームワーク・ライブラリ（全アプリで統一必須）
  "react": 18.2.0
  "react-dom": 18.2.0
  "next": 14.1.0
  
  # 開発ツール（バージョン統一で挙動一致）
  "typescript": 5.3.3
  "vitest": 1.2.0
  "@biomejs/biome": 1.5.0
  
  # 型定義（Node.jsバージョンと一致）
  "@types/node": 20.11.0
  
  # ビルドツール
  "wireit": 0.14.0
```

### カタログに含めないパッケージ

```json
{
  "dependencies": {
    // アプリ固有のユーティリティ
    "lodash": "4.17.21",
    
    // 特定機能のみ使用するライブラリ
    "chart.js": "4.4.0",
    
    // 後方互換性のないメジャーアップデートが多いもの
    "some-unstable-lib": "1.0.0"
  }
}
```

## Advanced Configuration

### 複数カタログ（named catalogs）

```yaml
catalog:
  # default catalog
  "react": 18.2.0

catalogs:
  # 特定用途用カタログ
  legacy:
    "react": 17.0.2
  
  next:
    "next": 14.1.0
    "@next/bundle-analyzer": 14.1.0
```

```json
{
  "dependencies": {
    "react": "catalog:",
    "next": "catalog:next"  // named catalog参照
  }
}
```

### injectWorkspacePackages と syncInjectedDepsAfterScripts

```yaml
injectWorkspacePackages: true

syncInjectedDepsAfterScripts:
  - build
```

この設定により:
1. ワークスペース内のパッケージが変更されると自動的に再リンク
2. `build`スクリプト実行後に依存関係が同期される

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| `catalog:`が解決されない | pnpmバージョンが古い | pnpm 9.5+にアップデート |
| strictモードでエラー | バージョンがカタログと不一致 | `pnpm-workspace.yaml`を修正 |
| workspaceパッケージがリンクされない | injectWorkspacePackages未設定 | `injectWorkspacePackages: true`を追加 |
| Node.jsバージョン不一致 | useNodeVersion設定 | `pnpm env use 20.11.0`で切り替え |

## Migration from Traditional Workspace

### Before

```json
// apps/app1/package.json
{
  "dependencies": {
    "react": "18.2.0"
  }
}

// apps/app2/package.json
{
  "dependencies": {
    "react": "18.1.0"  // バージョン不一致！
  }
}
```

### After

```yaml
# pnpm-workspace.yaml
catalog:
  "react": 18.2.0
catalogMode: strict
```

```json
// apps/app1/package.json
{
  "dependencies": {
    "react": "catalog:"
  }
}

// apps/app2/package.json
{
  "dependencies": {
    "react": "catalog:"
  }
}
```

## References

- [pnpm公式ドキュメント - Workspace Catalog](https://pnpm.io/catalogs)
- [pnpmワークスペース](https://pnpm.io/workspaces)
