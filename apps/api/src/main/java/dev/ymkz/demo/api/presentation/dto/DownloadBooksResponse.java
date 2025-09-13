package dev.ymkz.demo.api.presentation.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.ymkz.demo.api.domain.model.Book;

@JsonPropertyOrder({
    "id",
    "isbn",
    "title",
    "price",
    "status",
    "publishedAt",
    "authorId",
    "authorName",
    "publisherId",
    "publisherName",
    "createdAt",
    "updatedAt",
    "deletedAt",
})
public record DownloadBooksResponse(
        long id,
        String isbn,
        String title,
        Integer price,
        String status,
        String publishedAt,
        int authorId,
        String authorName,
        int publisherId,
        String publisherName) {
    /**
     * CSV出力用のフォーマットに変換
     *
     * Jacksonが扱えないLocalDateTimeをStringに変換している
     * ValueObjectもvalueを取得して扱う
     */
    public static DownloadBooksResponse from(Book book) {
        return new DownloadBooksResponse(
                book.id(),
                book.isbn().value(),
                book.title(),
                book.price(),
                book.status().name(),
                book.publishedAt() != null ? book.publishedAt().toString() : null,
                book.authorId(),
                book.authorName(),
                book.publisherId(),
                book.publisherName());
    }
}
