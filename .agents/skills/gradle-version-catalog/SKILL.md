---
name: gradle-version-catalog
description: Gradleプロジェクトで依存関係のバージョンを一元管理する場合に使用する。複数のサブプロジェクトで共通の依存関係を共有し、バージョンの不一致を防ぐために使用する。
license: MIT
compatibility: Gradle 7.0+, Kotlin DSL推奨
metadata:
  author: ymkz
  version: "1.0"
---

# Gradle Version Catalog

## Overview

Gradle Version Catalogを使用して、プロジェクト全体の依存関係バージョンを`gradle/libs.versions.toml`で一元管理する。

## When to Use

- マルチモジュールプロジェクトで依存関係バージョンを統一したい場合
- バージョン番号の重複を排除したい場合
- IDEでの補完と型安全性を確保したい場合
- プラグインとライブラリのバージョンを一元管理したい場合

## Directory Structure

```
gradle/
├── libs.versions.toml    # Version catalog定義
└── convention/           # Convention plugins（オプション）
settings.gradle.kts
build.gradle.kts
```

## Configuration

### 1. libs.versions.toml の作成

`gradle/libs.versions.toml`:

```toml
[versions]
spring-boot = "3.2.0"
junit = "5.10.0"
spotless = "6.23.0"

[libraries]
spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit" }

spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-starter-test = { module = "org.springframework.boot:spring-boot-starter-test" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter" }

[bundles]
unit-test = [
  "spring-boot-starter-test",
  "junit-jupiter"
]

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
```

### 2. カタログの参照

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
```

### 3. ビルドスクリプトでの使用

`build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spotless)
}

dependencies {
    // BOMによるバージョン管理
    implementation(platform(libs.spring.boot.bom))
    testImplementation(platform(libs.junit.bom))
    
    // 個別ライブラリ
    implementation(libs.spring.boot.starter.web)
    
    // Bundle（複数ライブラリのまとめ）
    testImplementation(libs.bundles.unit.test)
}
```

## Key Features

| 機能 | 説明 | 例 |
|------|------|-----|
| `[versions]` | バージョン番号の定義 | `spring-boot = "3.2.0"` |
| `[libraries]` | ライブラリの定義 | `spring-boot-starter-web = { ... }` |
| `[bundles]` | ライブラリのグループ化 | `unit-test = ["junit-jupiter", ...]` |
| `[plugins]` | Gradleプラグインの定義 | `spring-boot = { id = "...", version.ref = "..." }` |

## Best Practices

### 命名規約

```toml
# ✅ ハイフン区切り（Kebab case）
spring-boot-starter-web = { ... }

# ❌ キャメルケースやアンダースコアは避ける
springBootStarterWeb = { ... }  # NG
spring_boot_starter_web = { ... }  # NG
```

### BOM（Bill of Materials）の活用

```toml
[libraries]
# BOMを定義
spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }

# バージョンなしでライブラリを定義（BOMから解決）
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter.web)  // バージョン不要
}
```

### コメントでの出典明示

```toml
[versions]
spring-boot = "3.2.0" # https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-dependencies
junit = "5.10.0"      # https://mvnrepository.com/artifact/org.junit/junit-bom
```

## Common Patterns

### Convention Pluginとの組み合わせ

```kotlin
// gradle/convention/build.gradle.kts
dependencies {
    implementation(libs.spotless)  // catalogからプラグインを参照
}
```

```kotlin
// gradle/convention/src/main/kotlin/common-conventions.gradle.kts
plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        palantirJavaFormat()
    }
}
```

### マルチプロジェクトでの参照

```kotlin
// settings.gradle.kts
includeBuild("gradle/convention")
include(":apps:core", ":apps:api")
```

```kotlin
// apps/api/build.gradle.kts
plugins {
    id("common-conventions")  // convention plugin
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.api)
}
```

## Migration from Traditional Approach

### Before

```kotlin
// build.gradle.kts
buildscript {
    ext {
        set("springBootVersion", "3.2.0")
        set("junitVersion", "5.10.0")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:${property("springBootVersion")}")
}
```

### After

```toml
# gradle/libs.versions.toml
[versions]
spring-boot = "3.2.0"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.spring.boot.starter.web)
}
```

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| カタログが見つからない | ファイルパスが不正 | `gradle/libs.versions.toml`に配置 |
| IDEで補完が効かない | 設定の同期待ち | Gradleプロジェクトを再インポート |
| Bundleが動作しない | ライブラリ名の不一致 | toml内の名前と一致しているか確認 |
| Plugin aliasがエラー | プラグインIDの誤り | `plugins { alias(...) }`構文を確認 |

## References

- [Gradle公式ドキュメント - Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)
