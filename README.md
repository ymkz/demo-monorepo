# demo-monorepo

Spring Boot API、共有ドメイン、TanStack Start フロントエンドを同居させたデモ用モノレポ。

## 構成

```text
apps/
├── api       # Spring Boot REST API（Java / MyBatis / MySQL）
├── core      # 共有ドメインライブラリ（Java）
├── web-form  # TanStack Start フロントエンド（port 3000）
└── web-tool  # TanStack Start フロントエンド（port 4000）
```

## 必要ツール

このリポジトリは mise と pnpm を前提にしている。

- Java: Gradle toolchain で解決
- Node.js: `pnpm-workspace.yaml` の `useNodeVersion` で管理
- pnpm: `package.json` の `packageManager` で管理
- Docker: ローカル MySQL 起動に使用

## 初回セットアップ

```sh
CONFIG_TREE_DIR=/tmp/ymkz/demo-monorepo/configtree

install -d -m 700 "${CONFIG_TREE_DIR}"
printf '%s' 'demo_pass' > "${CONFIG_TREE_DIR}/DEMODB_DEMOUSER_PASSWORD"
printf '%s' 'root' > "${CONFIG_TREE_DIR}/MYSQL_ROOT_PASSWORD"
chmod 600 "${CONFIG_TREE_DIR}"/*

pnpm install --frozen-lockfile
```

ローカルクレデンシャルは `.env` ではなく config tree と Docker Compose secrets で読み込む。詳細は [ローカル環境セットアップ](./docs/development/setup-local.md) を参照。

## ローカル起動

### データベース

```sh
docker compose up -d --wait --wait-timeout=25
```

### API

```sh
./gradlew bootRun
```

### フロントエンド

```sh
pnpm dev
```

`pnpm dev` は Wireit 経由で `apps/web-form` と `apps/web-tool` を起動する。

## 検証コマンド

### フロントエンド

```sh
pnpm check
```

`pnpm check` は Biome、型チェック、ビルドを実行する。

### バックエンド

```sh
./gradlew build
```

`./gradlew build` はテスト、Spotless、統合テスト、OpenAPI 生成を含む。

## OpenAPI 運用

このプロジェクトはコードファーストで OpenAPI 仕様を生成する。

- 公開用仕様: `apps/api/src/main/resources/static/openapi/openapi.json`
- フロントエンド生成クライアント: `apps/*/src/generated/`

`apps/api/src/main/resources/static/openapi/` は公開ドキュメントとして扱うため例外的に Git 管理する。  
一方、`apps/*/src/generated/` は一時生成物のため Git 管理しない。

詳細は [OpenAPI 運用方針](./docs/development/openapi-policy.md) を参照。

## ドキュメント

- [API 仕様書](https://ymkz.github.io/demo-monorepo)
- [データベース](./docs/database/README.md)
- [開発](./docs/development/)

## 困ったとき

ローカル環境の再セットアップやクリーンアップ手順は [ローカル環境セットアップ](./docs/development/setup-local.md) を参照。
