# 単体テスト実装計画

## 目標
`@apps/api/` の単体テストカバレッジを現在の ~5% から **60%以上** に引き上げる

## 実装完了日: 2026-02-08

## 実装完了状況

### 作成したテストファイル

| # | テストクラス | テスト数 | 状態 |
|---|-------------|---------|------|
| 1 | BookSearchUsecaseTest.java | 4 | ✓ 完了 |
| 2 | BookDownloadUsecaseTest.java | 6 | ✓ 完了 |
| 3 | BookStatusTest.java | 4 | ✓ 完了 |
| 4 | BookSearchQueryTest.java | 2 | ✓ 完了 |
| 5 | BookEntityTest.java | 2 | ✓ 完了 |
| 6 | BookDatasourceTest.java | 4 | ✓ 完了 |

**合計**: 22件の新規テスト + 7件の既存テスト = **29件**

### テスト実行結果

```bash
./gradlew :apps:api:test
```

- **結果**: BUILD SUCCESSFUL
- **テスト数**: 29件
- **失敗**: 0件

### 実装内容詳細

#### Phase 1: Usecase層（完了）

## 実装済み機能のテスト優先度

### Phase 1: Usecase層（優先度: 高）

#### 1.1 BookSearchUsecaseTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/application/usecase/BookSearchUsecaseTest.java`
- **対象**: `BookSearchUsecase.java`
- **依存**: Mockitoで `BookRepository` をモック
- **テスト項目**:
  - 正常系: 検索クエリを実行してPaginated<Book>が返る
  - 境界値: offset=0, limit=1
  - 境界値: offset=Large, limit=Max(100)
  - 空結果: 検索条件に合致する書籍が0件

#### 1.2 BookDownloadUsecaseTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/application/usecase/BookDownloadUsecaseTest.java`
- **対象**: `BookDownloadUsecase.java`
- **依存**: Mockitoで `BookRepository` をモック
- **テスト項目**:
  - 正常系: CSV形式のバイト配列が返る
  - UTF-8 BOM: 先頭3バイトがEF BB BF
  - CSVヘッダー: 正しいヘッダー行が含まれる
  - データ行: 複数件の書籍データが正しくCSV変換される
  - 空結果: 0件の書籍でも空CSVが返る
  - 例外系: CSV変換失敗時のRuntimeException

---

### Phase 2: Domain層（優先度: 中）

#### 2.1 BookStatusTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/domain/model/BookStatusTest.java`
- **対象**: `BookStatus.java` (enum)
- **テスト項目**:
  - fromString: "UNPUBLISHED" → BookStatus.UNPUBLISHED
  - fromString: "PUBLISHED" → BookStatus.PUBLISHED
  - fromString: "OUT_OF_PRINT" → BookStatus.OUT_OF_PRINT
  - fromString不正値: nullまたは無効な文字列で例外

#### 2.2 BookSearchQueryTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/domain/model/BookSearchQueryTest.java`
- **対象**: `BookSearchQuery.java`
- **テスト項目**:
  - 全パラメータ指定での構築
  - 最小パラメータ（null多数）での構築
  - 値オブジェクトの正確性（Isbn, RangeInteger, RangeTime）

#### 2.3 BookCreateCommandTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/domain/model/BookCreateCommandTest.java`
- **対象**: `BookCreateCommand.java`
- **テスト項目**:
  - 正常なコマンド構築
  - バリデーション: 必須フィールド未指定時のエラー

#### 2.4 BookUpdateCommandTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/domain/model/BookUpdateCommandTest.java`
- **対象**: `BookUpdateCommand.java`
- **テスト項目**:
  - 正常なコマンド構築
  - 部分更新（一部フィールドのみ）

---

### Phase 3: Infrastructure層（優先度: 中〜低）

#### 3.1 BookEntityTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/infrastructure/datasource/BookEntityTest.java`
- **対象**: `BookEntity.java`
- **依存**: BookエンティティのtoBook()メソッド
- **テスト項目**:
  - toBook: 正常にBookモデルに変換される
  - toBook: nullフィールドの適切なハンドリング

#### 3.2 BookDatasourceTest.java
- **ファイル**: `src/test/java/dev/ymkz/demo/api/infrastructure/datasource/BookDatasourceTest.java`
- **対象**: `BookDatasource.java`
- **依存**: Mockitoで `BookMapper` をモック
- **テスト項目**:
  - search: count + list で正しいPaginated構築
  - download: list結果をBookリストに変換
  - findById/update/create/delete: 現在は空実装なので未テスト（実装完了後追加）

---

### Phase 4: Presentation層（優先度: 低）

#### 4.1 DTOクラスのテスト（オプション）
- `SearchBooksResponse.java`
- `FindBookByIdResponse.java`
- `CreateBookBody.java`
- `UpdateBookBody.java`
- `ErrorResponse.java`
- `DownloadBooksResponse.java`

**注**: Recordクラスで単純なデータ保持ならテスト優先度低。バリデーションや変換ロジックがあればテスト追加。

---

## 実装順序

| # | テストクラス | 見積もり | 理由 |
|---|------------|---------|------|
| 1 | BookSearchUsecaseTest | 15分 | 核心ビジネスロジック |
| 2 | BookDownloadUsecaseTest | 20分 | CSV生成ロジック検証 |
| 3 | BookStatusTest | 10分 | 単純なenumテスト |
| 4 | BookSearchQueryTest | 15分 | 値オブジェクトの組み合わせ |
| 5 | BookEntityTest | 10分 | 変換ロジック |
| 6 | BookDatasourceTest | 15分 | Repository実装 |
| 7 | BookCreateCommandTest | 10分 | 実装完了後 |
| 8 | BookUpdateCommandTest | 10分 | 実装完了後 |

**合計時間**: 約105分（1時間45分）

---

## テストライブラリ

依存関係は既に追加済み（`libs.bundles.unit.test`）:
- JUnit 5
- AssertJ
- Mockito
- Spring Boot Test

---

## 成功基準

- [ ] 全テストが `./gradlew test` でパス
- [ ] Jacocoカバレッジレポートで60%以上
- [ ] 新規テストファイルでspotlessCheckがパス
