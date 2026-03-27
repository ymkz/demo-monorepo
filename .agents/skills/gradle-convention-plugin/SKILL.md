---
name: gradle-convention-plugin
description: Gradleビルドロジックをconvention pluginとして共通化し、マルチモジュールプロジェクトでの設定重複を排除する場合に使用する。
license: MIT
compatibility: Gradle 8.0+, Kotlin DSL
metadata:
  author: ymkz
  version: "1.0"
---

# Gradle Convention Plugin

## Overview

GradleのConvention Pluginを使用して、共通のビルド設定をプラグインとして切り出し、複数のサブプロジェクトで共有する。

## When to Use

- 複数のサブプロジェクトで同じビルド設定を繰り返し定義している場合
- ビルドロジックの一元管理とDRY原則を適用したい場合
- ビルド設定の変更を一箇所で反映したい場合
- ビルドスクリプトの可読性と保守性を向上させたい場合

## Directory Structure

```
gradle/
├── convention/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── src/main/kotlin/
│       ├── common-conventions.gradle.kts
│       ├── java-conventions.gradle.kts
│       └── spring-boot-conventions.gradle.kts
├── libs.versions.toml
settings.gradle.kts
```

## Setup

### 1. Convention Plugin プロジェクトの作成

`gradle/convention/build.gradle.kts`:

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spotless)
}
```

### 2. Settings 設定

`gradle/convention/settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}
```

### 3. Root Settings でのインクルード

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "demo"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("gradle/convention")

include(":apps:core", ":apps:api")
```

## Convention Plugin の定義

### 基本例: common-conventions

`gradle/convention/src/main/kotlin/common-conventions.gradle.kts`:

```kotlin
plugins {
    java
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "dev.ymkz.demo"
version = "1.0.0"

spotless {
    java {
        palantirJavaFormat()
    }
    kotlinGradle {
        ktlint()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf("-Xlint:unchecked", "-Xlint:deprecation")
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("build") {
    dependsOn("spotlessApply")
}
```

### カスタムソースセットの追加

```kotlin
sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        java {
            setSrcDirs(listOf("src/intTest/java"))
        }
        resources {
            setSrcDirs(listOf("src/intTest/resources"))
        }
    }
}

configurations {
    named("intTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    named("intTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

tasks.register<Test>("intTest") {
    useJUnitPlatform()
    group = "verification"
    description = "Runs the integration-test suite."
    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
}

tasks.named("check") {
    dependsOn("intTest")
}
```

## サブプロジェクトでの使用

`apps/api/build.gradle.kts`:

```kotlin
plugins {
    id("common-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.springdoc.openapi)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.api)
    testImplementation(libs.bundles.unit.test)
    intTestImplementation(libs.bundles.integration.test)
    implementation(project(":apps:core"))
}
```

## パターン別実装例

### 階層的なConvention Plugin

```kotlin
// java-conventions.gradle.kts
plugins {
    id("common-conventions")
    `java-library`
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
}
```

```kotlin
// spring-boot-conventions.gradle.kts
plugins {
    id("java-conventions")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.api)
}
```

```kotlin
// apps/api/build.gradle.kts
plugins {
    id("spring-boot-conventions")
    alias(libs.plugins.springdoc.openapi)
}
```

### Precompiled Script Plugin（別ファイル分割）

```kotlin
// src/main/kotlin/java-quality.gradle.kts
plugins {
    checkstyle
    jacoco
}

checkstyle {
    toolVersion = "10.12.0"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
```

## Best Practices

### 命名規約

| プラグイン名 | 用途 |
|------------|------|
| `common-conventions` | 全プロジェクト共通の基本設定 |
| `java-conventions` | Javaプロジェクト向け設定 |
| `spring-boot-conventions` | Spring Bootプロジェクト向け設定 |
| `library-conventions` | ライブラリ公開向け設定 |

### 依存関係の注入

```kotlin
// ✅ 良い: プラグインに必要な依存のみをconventionに追加
// gradle/convention/build.gradle.kts
dependencies {
    implementation(libs.spotless)
    implementation(libs.spring.boot.gradle.plugin)
}

// ❌ 避ける: ライブラリ依存をここに含めない
// これらは各プロジェクトのbuild.gradle.ktsで定義
dependencies {
    // implementation(libs.spring.boot.starter.web)  // NG
}
```

### Version Catalogとの連携

```kotlin
// ✅ convention plugin内でもcatalogが使用可能
spotless {
    java {
        palantirJavaFormat()
    }
}

// カスタムタスクでの使用
tasks.register("showVersions") {
    doLast {
        println("Spring Boot: ${libs.versions.spring.boot.get()}")
    }
}
```

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| プラグインが見つからない | includeBuildされていない | rootのsettings.gradle.ktsを確認 |
| catalogが参照できない | conventionのsettings設定不足 | versionCatalogs設定を確認 |
| 変更が反映されない | ビルドキャッシュ | `./gradlew --stop`後再実行 |
| IDEでエラー表示 | Gradle同期待ち | Gradleプロジェクトを再インポート |

## Comparison: Before vs After

### Before: 重複した設定

```kotlin
// apps/api/build.gradle.kts
plugins {
    java
    id("com.diffplug.spotless") version "6.23.0"
}
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
spotless { java { palantirJavaFormat() } }

tasks.withType<Test> { useJUnitPlatform() }

// apps/core/build.gradle.kts（同じ設定の繰り返し）
plugins {
    java
    id("com.diffplug.spotless") version "6.23.0"
}
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
spotless { java { palantirJavaFormat() } }

tasks.withType<Test> { useJUnitPlatform() }
```

### After: DRYな設定

```kotlin
// gradle/convention/src/main/kotlin/common-conventions.gradle.kts
plugins {
    java
    id("com.diffplug.spotless")
}
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
spotless { java { palantirJavaFormat() } }
tasks.withType<Test> { useJUnitPlatform() }

// apps/api/build.gradle.kts
plugins {
    id("common-conventions")
}

// apps/core/build.gradle.kts
plugins {
    id("common-conventions")
}
```

## References

- [Gradle公式ドキュメント - Convention Plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html)
- [Gradle Plugin Development](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [Precompiled Script Plugins](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:precompiled_plugins)
