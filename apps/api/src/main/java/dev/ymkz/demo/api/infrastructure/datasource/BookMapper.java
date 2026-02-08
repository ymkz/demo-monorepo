package dev.ymkz.demo.api.infrastructure.datasource;

import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookMapper {

    @Select("""
        <script>
        SELECT
            COUNT(1)
        FROM
            books as b
        INNER JOIN
            (
                SELECT id, author_name
                FROM authors
                WHERE deleted_at IS NULL
            ) as a ON b.author_id = a.id
        INNER JOIN
            (
                SELECT id, publisher_name
                FROM publishers
                WHERE deleted_at IS NULL
            ) as p ON b.publisher_id = p.id
        WHERE
            b.deleted_at IS NULL
            <if test="isbn != null">
                AND b.isbn = #{isbn.value}
            </if>
            <if test="title != null">
                AND b.title LIKE '%' || #{title} || '%'
            </if>
            <if test="priceRange.min != null">
                AND b.price &gt;= #{priceRange.min}
            </if>
            <if test="priceRange.max != null">
                AND b.price &lt;= #{priceRange.max}
            </if>
            <if test="statuses != null">
                AND b.status IN
                <foreach collection="statuses" item="status" separator="," open="(" close=")">
                    #{status}
                </foreach>
            </if>
            <if test="publishedAtRange.start != null">
                AND b.published_at &gt;= #{publishedAtRange.start}
            </if>
            <if test="publishedAtRange.end != null">
                AND b.published_at &lt;= #{publishedAtRange.end}
            </if>
        </script>
    """)
    int count(BookSearchQuery query);

    @Select("""
        <script>
        SELECT
            b.id,
            b.isbn,
            b.title,
            b.price,
            b.status,
            b.published_at,
            b.author_id,
            a.author_name,
            b.publisher_id,
            p.publisher_name,
            b.created_at,
            b.updated_at,
            b.deleted_at
        FROM
            books as b
        INNER JOIN
            (
                SELECT id, author_name
                FROM authors
                WHERE deleted_at IS NULL
            ) as a ON b.author_id = a.id
        INNER JOIN
            (
                SELECT id, publisher_name
                FROM publishers
                WHERE deleted_at IS NULL
            ) as p ON b.publisher_id = p.id
        WHERE
            b.deleted_at IS NULL
            <if test="isbn != null">
                AND b.isbn = #{isbn.value}
            </if>
            <if test="title != null">
                AND b.title LIKE '%' || #{title} || '%'
            </if>
            <if test="priceRange.min != null">
                AND b.price &gt;= #{priceRange.min}
            </if>
            <if test="priceRange.max != null">
                AND b.price &lt;= #{priceRange.max}
            </if>
            <if test="statuses != null">
                AND b.status IN
                <foreach collection="statuses" item="status" separator="," open="(" close=")">
                    #{status}
                </foreach>
            </if>
            <if test="publishedAtRange.start != null">
                AND b.published_at &gt;= #{publishedAtRange.start}
            </if>
            <if test="publishedAtRange.end != null">
                AND b.published_at &lt;= #{publishedAtRange.end}
            </if>
        ORDER BY
            #{order.orderBy}
        <if test="limit != null">
            LIMIT #{limit}
        </if>
        <if test="offset != null">
            OFFSET #{offset}
        </if>
        </script>
    """)
    List<BookEntity> list(BookSearchQuery query);
}
