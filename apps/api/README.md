# apps/api

## パッケージ構成

`apps/api` では package-by-feature を採用する。

```text
src/main/java/dev/ymkz/demo/api
├── features
│   └── <feature>
│       ├── application
│       ├── domain
│       ├── infrastructure
│       └── presentation
├── shared
│   ├── exception
│   └── logging
├── config
│   └── mybatis
└── external
    └── <service>
```

## 配置ルール

### features
機能ごとのコードをまとめる。

例:
- `features/books/application`
- `features/books/domain`
- `features/books/infrastructure`
- `features/books/presentation`

新しいAPI機能を追加する場合は、まず `features/<feature>` を作成し、その配下に責務別パッケージを置く。

### shared
複数featureで共有するアプリケーション共通部を置く。

例:
- 例外ハンドラ
- リクエスト横断のロギング

### config
フレームワークやミドルウェアの設定を置く。

例:
- MyBatis設定
- メトリクスInterceptor

### external
外部サービス連携を置く。

例:
- JSONPlaceholderクライアント

## テスト構成

```text
src/test/java/dev/ymkz/demo/api
├── features
│   └── <feature>
└── shared

src/intTest/java/dev/ymkz/demo/api
├── features
│   └── <feature>
└── support
    └── testing
```

- 単体テストは本体パッケージ構成を原則ミラーする
- 統合テストは `features/<feature>` 配下に置く
- Testcontainersなどのテスト共通設定は `support/testing` に置く

## 命名方針

- feature名は複数形で統一する
  - 例: `books`, `authors`, `publishers`
- DTOは feature 配下の `presentation/dto` に置く
- Repositoryインターフェースは feature 配下の `domain` に置く
- datasource / mapper / entity は feature 配下の `infrastructure` に置く

## 避けること

- `presentation`, `application`, `domain`, `infrastructure` をルート直下に横断配置しない
- feature固有DTOを `shared` に置かない
- feature固有の永続化実装を `config` や `shared` に置かない
