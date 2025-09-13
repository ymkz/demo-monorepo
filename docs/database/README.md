# データベース

- [demo](./schema/README.md)

## データベーススキーマのドキュメントの更新手順

```sh { name=update-db-docs }
REPOSITORY_ROOT=$(git rev-parse --show-toplevel)
tbls doc mysql://demo_user:${DEMODB_DEMOUSER_PASSWORD}@localhost:3306/demo_db "${REPOSITORY_ROOT}/docs/database/schema" --force
```

上記コマンドを実行することでスキーマの情報が[docs/database/README.md](../database/README.md)に出力されます。
