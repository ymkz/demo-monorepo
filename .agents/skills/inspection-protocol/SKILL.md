---
name: inspection-protocol
description: コード変更後、必ず検証コマンドを実行してから完了宣言をする場合に使用する。GradleプロジェクトではspotlessCheck/test/build/intTestを、pnpmプロジェクトではbuild/lint/testを実行し、すべてパスすることを確認する。
license: MIT
compatibility: Gradle 8.0+, pnpm 9.5+, Node.js 18+
metadata:
  author: ymkz
  version: "1.0"
---

# Verification Before Completion

## Overview

コード変更を行った後、必ず検証コマンドを実行してから「完了しました」と宣言する。検証がパスするまでは作業は完了していない。

## When to Use

- コード変更を行った後
- 「修正しました」「完了しました」と報告する前
- PRを作成する前
- どの検証コマンドを実行すべきか迷った場合

## Iron Rule

**検証がパスするまでは作業は完了していない。**

```
❌ "修正しました。ビルドは通ると思います。"
✅ "修正しました。./gradlew build がパスしました。"
```

## Verification Commands

### Gradle Backend Projects

```bash
# 1. コードフォーマットチェック
./gradlew :apps:<name>:spotlessCheck

# 2. 単体テスト
./gradlew :apps:<name>:test

# 3. 統合テスト（intTestソースセットがある場合）
./gradlew :apps:<name>:intTest

# 4. ビルド（上記すべてを含む）
./gradlew :apps:<name>:build

# 5. すべてのチェック（推奨）
./gradlew :apps:<name>:check
```

#### Format Auto-fix

```bash
# 自動修正可能な場合
./gradlew :apps:<name>:spotlessApply

# その後、再度チェック
./gradlew :apps:<name>:spotlessCheck
```

### pnpm Frontend Projects

```bash
# 1. リントチェック
pnpm lint

# 2. 型チェック
pnpm typecheck

# 3. ビルド
pnpm build

# 4. テスト
pnpm test

# 5. すべてのチェック（推奨）
pnpm check
```

#### Auto-fix

```bash
# 自動修正
pnpm format

# または
pnpm lint --write
```

## Workflow

### Step 1: 変更を行う

```kotlin
// コード修正
fun calculateTotal(price: Int, quantity: Int): Int {
    return price * quantity  // 修正済み
}
```

### Step 2: 検証コマンドを実行

```bash
# Gradleプロジェクトの場合
./gradlew :apps:api:check

# 出力例：
# BUILD SUCCESSFUL in 45s
# 15 actionable tasks: 15 executed
```

### Step 3: 結果を確認

```bash
# すべてパスした場合
BUILD SUCCESSFUL

# 失敗した場合
BUILD FAILED
# エラーを修正して再度実行
```

### Step 4: 完了を宣言

```
✅ 修正が完了し、すべての検証がパスしました。
   - ./gradlew :apps:api:spotlessCheck ✓
   - ./gradlew :apps:api:test ✓
   - ./gradlew :apps:api:intTest ✓
   - ./gradlew :apps:api:build ✓
```

## Common Mistakes

### ❌ 検証スキップ

```
ユーザー: "このファイルを修正してください"

❌ アシスタント: "修正しました。"
   （検証なし）

✅ アシスタント: "修正しました。検証を実行します..."
   ./gradlew :apps:api:test
   BUILD SUCCESSFUL
   "すべてのテストがパスしました。"
```

### ❌ 部分的な検証

```bash
# ❌ 避ける: テストのみ実行
./gradlew :apps:api:test

# ✅ 推奨: すべてのチェック
./gradlew :apps:api:check
```

### ❌ エラーを無視

```bash
$ ./gradlew :apps:api:test

# エラーが出ても無視して「完了」としない
❌ "修正しました。"

# エラーを修正して再実行
✅ エラーを修正 → 再実行 → パスを確認 → "完了しました"
```

## Project-Specific Verification

### API Application (Gradle)

```bash
./gradlew :apps:api:spotlessCheck
./gradlew :apps:api:test
./gradlew :apps:api:intTest
./gradlew :apps:api:build
```

### Core Module (Gradle)

```bash
./gradlew :apps:core:check
```

### Web Form (pnpm)

```bash
cd apps/web-form
pnpm check
```

### Web Tool (pnpm)

```bash
cd apps/web-tool
pnpm check
```

## Troubleshooting

| 問題 | 対応 |
|------|------|
| spotlessCheck失敗 | `./gradlew :apps:<name>:spotlessApply` → 再実行 |
| テスト失敗 | エラーメッセージを確認 → 修正 → 再実行 |
| 型チェック失敗 | TypeScriptエラーを修正 → `pnpm typecheck`再実行 |
| ビルド失敗 | 依存関係を確認 → `pnpm install` → 再ビルド |

## References

- [Gradle Check Task](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks)
- [pnpm Scripts](https://pnpm.io/cli/run)
