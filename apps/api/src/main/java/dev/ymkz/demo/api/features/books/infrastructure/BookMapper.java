package dev.ymkz.demo.api.features.books.infrastructure;

import dev.ymkz.demo.api.features.books.domain.BookCreateCommand;
import dev.ymkz.demo.api.features.books.domain.BookSearchQuery;
import dev.ymkz.demo.api.features.books.domain.BookUpdateCommand;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
            <choose>
                <when test='order != null and order.name() == "PRICE_ASC"'>
                    b.price ASC
                </when>
                <when test='order != null and order.name() == "PRICE_DESC"'>
                    b.price DESC
                </when>
                <when test='order != null and order.name() == "PUBLISHED_AT_ASC"'>
                    b.published_at ASC
                </when>
                <otherwise>
                    b.published_at DESC
                </otherwise>
            </choose>
        <if test="limit != null">
            LIMIT #{limit}
        </if>
        <if test="offset != null">
            OFFSET #{offset}
        </if>
        </script>
    """)
    List<BookEntity> list(BookSearchQuery query);

    @Select("""
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
            authors as a ON b.author_id = a.id AND a.deleted_at IS NULL
        INNER JOIN
            publishers as p ON b.publisher_id = p.id AND p.deleted_at IS NULL
        WHERE
            b.id = #{id}
            AND b.deleted_at IS NULL
        """)
    Optional<BookEntity> findById(long id);

    @Insert("""
        INSERT INTO books (
            isbn,
            title,
            price,
            status,
            published_at,
            author_id,
            publisher_id
        ) VALUES (
            #{isbn.value},
            #{title},
            #{price},
            #{status},
            #{publishedAt},
            #{authorId},
            #{publisherId}
        )
        """)
    int create(BookCreateCommand command);

    @Update("""
        <script>
        UPDATE
            books
        <set>
            updated_at = CURRENT_TIMESTAMP,
            <if test="isbn != null">
                isbn = #{isbn.value},
            </if>
            <if test="title != null">
                title = #{title},
            </if>
            <if test="price != null">
                price = #{price},
            </if>
            <if test="status != null">
                status = #{status},
            </if>
            <if test="publishedAt != null">
                published_at = #{publishedAt},
            </if>
            <if test="authorId != null">
                author_id = #{authorId},
            </if>
            <if test="publisherId != null">
                publisher_id = #{publisherId},
            </if>
        </set>
        WHERE
            id = #{id}
            AND deleted_at IS NULL
        </script>
        """)
    int update(BookUpdateCommand command);

    @Update("""
        UPDATE
            books
        SET
            deleted_at = CURRENT_TIMESTAMP
        WHERE
            id = #{id}
            AND deleted_at IS NULL
        """)
    int delete(long id);
}
