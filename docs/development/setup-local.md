# ローカル環境セットアップ

## 最初のセットアップ

ローカル環境のクレデンシャルは `.env` ではなく Spring Boot の config tree と Docker Compose secrets で読み込む。

このプロジェクトのローカル開発では、config tree の配置先を `/tmp/ymkz/demo-monorepo/configtree/` に統一する。

```sh { name=setup-local }
CONFIG_TREE_DIR=/tmp/ymkz/demo-monorepo/configtree

install -d -m 700 "${CONFIG_TREE_DIR}"
printf '%s' 'demo_pass' > "${CONFIG_TREE_DIR}/DEMODB_DEMOUSER_PASSWORD"
printf '%s' 'root' > "${CONFIG_TREE_DIR}/MYSQL_ROOT_PASSWORD"
chmod 600 "${CONFIG_TREE_DIR}"/*

pnpm install --frozen-lockfile
```

`/tmp` 配下は OS の再起動やクリーンアップで削除される可能性がある。Docker Compose や API 起動時に secret ファイルが見つからない場合は、上記のセットアップを再実行する。

## 読み込み方式

- Spring Boot: `spring.config.import=optional:configtree:/tmp/ymkz/demo-monorepo/configtree/`
- Docker Compose: MySQL 公式イメージの `*_FILE` 環境変数と Compose secrets

## 困ったときは

npm の依存やビルド出力による成果物のディレクトリなどを一度すべて削除してみる。\
再度`pnpm install --frozen-lockfile`すること。\
また lockfile も消すため依存ライブラリが内部で依存する詳細な依存バージョンは変わる可能性があることに注意。

```sh { name=clean-local }
REPOSITORY_ROOT=$(git rev-parse --show-toplevel)

find "${REPOSITORY_ROOT}" -name '.gradle' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name '.next' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name '.wireit' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name 'build' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name 'coverage' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name 'dist' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name 'node_modules' -type d -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name 'pnpm-lock.yaml' -type f -prune -exec rm -rf '{}' +
find "${REPOSITORY_ROOT}" -name 'tsconfig.tsbuildinfo' -type f -prune -exec rm -rf '{}' +
```
