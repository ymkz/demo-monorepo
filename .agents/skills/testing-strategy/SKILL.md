---
name: testing-strategy
description: 単体テストとインテグレーションテストを使い分ける場合に使用する。テストピラミッドに基づき、テストの責務、実行速度、メンテナンスコストを考慮して適切なテスト戦略を選択する。
license: MIT
compatibility: Java, Kotlin, TypeScript, JUnit 5, Vitest
metadata:
  author: ymkz
  version: "1.0"
---

# Testing Strategy

## Overview

単体テスト（Unit Test）とインテグレーションテスト（Integration Test）を明確に使い分ける。テストピラミッドに基づき、責務、実行速度、メンテナンスコストを考慮して適切なテスト戦略を選択する。

## When to Use

- 新しい機能のテストを書く際、どちらのテストを選ぶか迷った場合
- 既存のテストが重くなりすぎてきた場合
- テスト戦略を見直したい場合
- 「このテストは本当にインテグレーションテストが必要か？」と疑問に思った場合

## Test Pyramid

```
        /\
       /  \
      / E2E\        <- 少数（UIテスト、E2Eテスト）
     /______\
    /        \
   /Integration\   <- 中程度（インテグレーションテスト）
  /______________\
 /                \
/      Unit        \  <- 多数（単体テスト）
/____________________\
```

**基本原則**: 下層（単体テスト）を厚く、上層（インテグレーション/E2E）を薄く保つ。

## Test Types

### 単体テスト（Unit Test）

**定義**: 1つのクラス/メソッドを対象とし、依存関係はモック化する

**配置**: `src/test`

**特徴**:
- 実行時間: ミリ秒〜秒単位（高速）
- 外部依存: なし（DB、HTTP、ファイルシステムなどをモック）
- 失敗時の原因特定: 容易
- メンテナンスコスト: 低い

**書くべきケース**:
- ビジネスロジックの検証
- 条件分岐の網羅的テスト
- 境界値の検証
- エラーハンドリングの検証

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void 存在する書籍IDで取得すると書籍が返されること() {
        // Given: モックの設定
        var bookId = 1L;
        var expected = new Book(bookId, "Spring in Action");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(expected));

        // When: テスト対象の実行
        var result = bookService.findById(bookId);

        // Then: 検証
        assertThat(result).isEqualTo(expected);
        verify(bookRepository).findById(bookId);
    }

    @Test
    void 存在しない書籍IDで取得すると例外が発生すること() {
        // Given
        var bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            bookService.findById(bookId);
        });
    }
}
```

### インテグレーションテスト（Integration Test）

**定義**: 複数のコンポーネントや外部システム（DB、HTTP等）を組み合わせて動作を検証する

**配置**: `src/intTest`

**特徴**:
- 実行時間: 秒〜分単位（遅い）
- 外部依存: あり（TestContainersでDB等を起動）
- 失敗時の原因特定: 複雑（どの層の問題か特定が難しい）
- メンテナンスコスト: 高い（スキーマ変更等の影響を受けやすい）

**書くべきケース**:
- APIエンドポイントの動作確認
- DBアクセスの統合確認
- 外部API連携の確認
- トランザクション動作の確認

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class BookControllerIntTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 書籍一覧APIを呼び出すと200と書籍リストが返されること() throws Exception {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void 存在しない書籍IDで取得すると404が返されること() throws Exception {
        mockMvc.perform(get("/api/books/99999"))
            .andExpect(status().isNotFound());
    }
}
```

## Decision Matrix

| 検証したいこと | 単体テスト | インテグレーションテスト |
|---------------|-----------|----------------------|
| 計算ロジックの正確性 | ✅ | ❌ |
| 条件分岐の網羅 | ✅ | ❌ |
| DBスキーマとの整合性 | ❌ | ✅ |
| APIレスポンスの形式 | ❌ | ✅ |
| トランザクション境界 | ❌ | ✅ |
| バリデーションルール | ✅ | ⚠️ |
| SQLクエリの正確性 | ⚠️ | ✅ |

## Best Practices

### ✅ 推奨パターン

**1. ビジネスロジックは単体テストで網羅的に**

```java
@Test
void 税率8パーセントの場合の計算が正しいこと() {
    var calculator = new TaxCalculator(new BigDecimal("0.08"));
    
    assertThat(calculator.calculate(new BigDecimal("100")))
        .isEqualByComparingTo(new BigDecimal("108"));
}

@Test
void 税率10パーセントの場合の計算が正しいこと() {
    var calculator = new TaxCalculator(new BigDecimal("0.10"));
    
    assertThat(calculator.calculate(new BigDecimal("100")))
        .isEqualByComparingTo(new BigDecimal("110"));
}
```

**2. インテグレーションテストは「正常系の代表的なケース」に絞る**

```java
@Test
void 書籍登録APIで正常に登録できること() {
    // 正常系の1ケースのみ
    // 異常系（バリデーション等）は単体テストで網羅
}
```

**3. テストデータの管理**

```java
// ✅ 良い: 各テストで独立したデータを用意
@Test
void テストA() {
    var book = bookRepository.save(new Book("テスト書籍A"));
    // テスト実行
}

@Test
void テストB() {
    var book = bookRepository.save(new Book("テスト書籍B"));
    // テスト実行
}
```

### ❌ アンチパターン

**1. 単体テストで実際のDBを使用**

```java
// ❌ 避ける
@Test
void 単体テストなのに実際のDBにアクセスしている() {
    var repository = new BookRepository(dataSource);  // 実際のDB接続
    // これはインテグレーションテスト
}
```

**2. インテグレーションテストで条件分岐を網羅**

```java
// ❌ 避ける: インテグレーションテストで多くのパターンを網羅
@SpringBootTest
class BookServiceIntTest {
    @Test
    void パターン1() { }
    @Test
    void パターン2() { }
    @Test
    void パターン3() { }
    // ... 10個以上のテスト
}
```

**3. 単体テストをインテグレーションテストの代わりにしない**

```java
// ❌ 避ける: インテグレーションテストが必要なのにモックでごまかす
@Test
void SQLの動作をモックで検証() {
    when(repository.findByTitle("Spring")).thenReturn(List.of(book));
    // これではSQLの正確性は検証できない
}
```

## Repository-Specific Patterns

### このリポジトリでの実践

**単体テスト（`src/test`）**:
- Usecase層のビジネスロジック
- Domainモデルの振る舞い
- Validationロジック
- ユーティリティ関数

**インテグレーションテスト（`src/intTest`）**:
- ControllerのAPI動作
- MyBatis MapperのSQL実行
- トランザクション制御
- 外部API連携

### 実行コマンド

```bash
# 単体テストのみ（高速）
./gradlew :apps:api:test

# インテグレーションテスト（遅い）
./gradlew :apps:api:intTest

# すべて
./gradlew :apps:api:check
```

## Troubleshooting

| 問題 | 原因 | 解決策 |
|------|------|--------|
| テストが遅い | インテグレーションテストが多すぎる | 単体テストに置き換え可能か検討 |
| テストが不安定 | 外部依存の影響 | モックを使用した単体テストに変更 |
| メンテナンスが大変 | インテグレーションテストの過多 | テストピラミッドを見直す |
| カバレッジが低い | 単体テストが不足 | ビジネスロジックの単体テストを追加 |

## References

- [Test Pyramid - Martin Fowler](https://martinfowler.com/articles/practical-test-pyramid.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [TestContainers](https://www.testcontainers.org/)
