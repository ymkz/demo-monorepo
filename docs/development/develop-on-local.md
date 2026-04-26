# ローカル環境での開発

## Docker Compose で外部依存を立ち上げる

Docker Compose を利用してアプリケーションが依存する MySQL データベースを再現する。

### 起動する

```sh { name=compose-up }
docker compose up -d --wait --wait-timeout=25
```

### 停止する

```sh { name=compose-down }
docker compose down
```

### MySQL コンテナへ接続する

```sh { name=exec-mysql }
docker compose exec demo-monorepo-db sh -c 'mysql demo_db -udemo_user -p$(cat /run/secrets/demodb_demouser_password)'
```
