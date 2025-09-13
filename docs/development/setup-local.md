# ローカル環境セットアップ

## 最初のセットアップ

ローカル環境では mise を使って`.env`を自動的に読み込み環境変数を展開する。\
実行時に必要となるクレデンシャルはこれを利用して参照する。

```sh { name=setup-local }
REPOSITORY_ROOT=$(git rev-parse --show-toplevel)

cat << EOS >> "${REPOSITORY_ROOT}/.env"
DEMODB_DEMOUSER_PASSWORD=demo_pass
EOS
```

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
