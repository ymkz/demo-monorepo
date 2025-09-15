CREATE TABLE authors (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '著者ID（自動採番）',
  `author_name` varchar(128) NOT NULL COMMENT '著者名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'レコード作成時刻',
  `updated_at` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'レコード更新時刻',
  `deleted_at` datetime NULL COMMENT 'レコード論理削除時刻',
  PRIMARY KEY (`id`)
) COMMENT '著者の情報を格納するテーブル';

CREATE TABLE publishers (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '出版社ID（自動採番）',
  `publisher_name` varchar(128) NOT NULL COMMENT '出版社名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'レコード作成時刻',
  `updated_at` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'レコード更新時刻',
  `deleted_at` datetime NULL COMMENT 'レコード論理削除時刻',
  PRIMARY KEY (`id`)
) COMMENT '出版社の情報を格納するテーブル';

CREATE TABLE book_status (
  `value` varchar(48) NOT NULL COMMENT '書籍ステータスの値',
  `description` varchar(96) NOT NULL COMMENT '書籍ステータスの説明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'レコード作成時刻',
  `updated_at` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'レコード更新時刻',
  `deleted_at` datetime NULL COMMENT 'レコード論理削除時刻',
  PRIMARY KEY (`value`)
) COMMENT '書籍のステータスマスタを格納するテーブル';

CREATE TABLE books (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '書籍ID（自動採番）',
  `isbn` varchar(13) NOT NULL COMMENT 'ISBNコード',
  `title` varchar(512) NOT NULL COMMENT '書籍タイトル',
  `price` int NULL COMMENT '価格',
  `status` varchar(24) NOT NULL COMMENT '書籍ステータス',
  `published_at` datetime NULL COMMENT '出版日',
  `author_id` int NOT NULL COMMENT '著者ID（authorsテーブル参照）',
  `publisher_id` int NOT NULL COMMENT '出版社ID（publishersテーブル参照）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'レコード作成時刻',
  `updated_at` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'レコード更新時刻',
  `deleted_at` datetime NULL COMMENT 'レコード論理削除時刻',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`status`) REFERENCES book_status(`value`),
  FOREIGN KEY (`author_id`) REFERENCES authors(`id`),
  FOREIGN KEY (`publisher_id`) REFERENCES publishers(`id`)
) COMMENT '書籍の基本情報を格納するテーブル';
