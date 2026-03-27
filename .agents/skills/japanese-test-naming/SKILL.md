---
name: japanese-test-naming
description: テストメソッド名やテストケース名に日本語を使用し、仕様の意図を明確に表現する場合に使用する。 Given-When-Thenパターンを日本語で表現することで、非技術者とのコミュニケーションも円滑にする。
license: MIT
compatibility: Java, Kotlin, JUnit 5, Vitest, Jest
metadata:
  author: ymkz
  version: "1.0"
---

# Japanese Test Naming

## Overview

テストメソッド名やテストケース名に日本語を使用し、テストの意図を明確に表現する。技術的な命名規則よりも、仕様や振る舞いを自然言語で説明することを優先する。

## When to Use

- テストの意図を日本語で明確に表現したい場合
- 非技術者との要件確認にテストを活用したい場合
- 英語での命名が不明瞭になりがちな場合
- チームの母語が日本語である場合
- ドメイン知識を正確に表現したい場合

## Naming Patterns

### Basic Pattern

```
{テスト対象}_{条件}_{期待結果}
```

### Java / Kotlin Examples

```kotlin
// ❌ 避ける: 英語の省略形や曖昧な命名
fun test1() { }
fun createBookTest() { }
fun shouldCreateBookWhenValid() { }

// ✅ 良い: 日本語で意図を明確に
fun 有効な書籍情報で作成すると保存されること() { }
fun ISBNが未指定の場合はエラーが返されること() { }
fun タイトルが100文字を超える場合はバリデーションエラーになること() { }
```

### TypeScript / Vitest Examples

```typescript
// ❌ 避ける
test('test', () => {});
test('create book', () => {});

// ✅ 良い
test('有効な書籍情報で作成すると保存されること', () => {});
test('ISBNが未指定の場合はエラーが返されること', () => {});
```

## Given-When-Then in Japanese

### パターン: {前提条件}_{操作}_{期待結果}

```kotlin
@Test
fun 書籍が存在しない状態で検索すると空リストが返されること() {
    // Given: 書籍が存在しない状態で
    
    // When: 検索すると
    val result = bookRepository.search("Spring");
    
    // Then: 空リストが返されること
    assertThat(result).isEmpty();
}
```

### バリデーションテスト

```kotlin
@Test
fun タイトルが空文字の場合はバリデーションエラーになること() {
    val request = BookCreateRequest(title = "", isbn = "1234567890");
    
    val violations = validator.validate(request);
    
    assertThat(violations).hasSize(1);
    assertThat(violations.first().message).isEqualTo("タイトルは必須です");
}
```

### エラーハンドリングテスト

```kotlin
@Test
fun 存在しない書籍IDで取得すると404エラーが返されること() {
    val nonExistentId = 99999L;
    
    assertThrows<NotFoundException> {
        bookService.findById(nonExistentId);
    }.also {
        assertThat(it.message).contains("書籍が見つかりません");
    };
}
```

## Parameterized Tests

### JUnit 5 with Japanese Display Names

```kotlin
@ParameterizedTest(name = "{0}の場合")
@CsvSource(
    "空文字, ''",
    "空白のみ, '   '",
    "null, null"
)
fun 無効なタイトルはバリデーションエラーになること(
    description: String,
    title: String?
) {
    val request = BookCreateRequest(title = title, isbn = "1234567890");
    
    val violations = validator.validate(request);
    
    assertThat(violations).isNotEmpty();
}
```

### Vitest with Japanese Test Names

```typescript
describe('書籍検索', () => {
  test.each([
    ['キーワードに一致する書籍が存在する場合', 'Spring', 3],
    ['キーワードに一致する書籍が存在しない場合', 'Unknown', 0],
    ['空文字で検索した場合', '', 10],
  ])('%s: %sを検索すると%i件返されること', async (_, keyword, expected) => {
    const result = await searchBooks(keyword);
    expect(result.length).toBe(expected);
  });
});
```

## Best Practices

### ✅ 推奨パターン

```kotlin
// 仕様をそのまま表現
fun 新規登録時に自動で作成日時が設定されること()

// 境界値を明示
fun タイトルがちょうど100文字の場合は正常に保存されること()
fun タイトルが101文字の場合はバリデーションエラーになること()

// 状態遷移を明示
fun 未公開の書籍を公開すると公開日時が設定されること()
```

### ❌ 避けるべきパターン

```kotlin
// 実装詳細に依存
fun bookRepositorySaveTest()  // Repositoryの実装をテストしているだけ

// 不完全な日本語
fun テスト()  // 何をテストしているか不明

// 英語の直訳
fun bookCreateWhenValidThenSuccess()  // 機械的な英語パターン
```

## Framework Configuration

### JUnit 5 (Java/Kotlin)

```kotlin
// build.gradle.kts
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
```

テストレポートに日本語が正しく表示される。

### Vitest

```typescript
// vitest.config.ts
export default defineConfig({
  test: {
    name: '日本語テスト',
    include: ['src/**/*.test.ts'],
    reporters: ['verbose'],  // 日本語テスト名を詳細表示
  },
});
```

### Gradle Test Output

```kotlin
// 日本語テスト名を含む出力
 tasks.test {
    testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        events("PASSED", "FAILED", "SKIPPED")
        
        // 日本語を正しく表示
        outputs.upToDateWhen { false }
    }
}
```

## IDE Support

### IntelliJ IDEA / Android Studio

日本語テスト名はそのまま表示される:

```
✓ 有効な書籍情報で作成すると保存されること
✓ ISBNが未指定の場合はエラーが返されること
✗ タイトルが空文字の場合はバリデーションエラーになること
```

### VS Code with Vitest Extension

```
RUN  v1.0.0 /path/to/project

✓ apps/web-form/src/tests/book.test.ts (3 tests) 45ms
  ✓ 書籍検索 (3 tests)
    ✓ キーワードに一致する書籍が存在する場合: Springを検索すると3件返されること
    ✓ キーワードに一致する書籍が存在しない場合: Unknownを検索すると0件返されること
    ✓ 空文字で検索した場合: 空文字を検索すると10件返されること
```

## Communication Benefits

### テスト名から仕様が読める

```kotlin
class BookSearchTest {
    fun キーワードに部分一致する書籍が返されること() { }
    fun 大文字小文字を区別しないこと() { }
    fun 検索結果は出版日の降順でソートされること() { }
    fun 最大100件まで返されること() { }
    fun 検索結果が0件の場合は空リストを返すこと() { }
}
```

これらのテスト名から、書籍検索機能の仕様が一目で理解できる。

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| CIで文字化け | 文字エンコーディング | UTF-8設定を確認 |
| テストレポートで文字化け | フォント問題 | 日本語フォントをインストール |
| IDEで警告 | メソッド名規約 | 無視して構わない |

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Vitest Documentation](https://vitest.dev/)
