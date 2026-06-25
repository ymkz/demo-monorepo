package dev.ymkz.demo.api.features.books;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import dev.ymkz.demo.api.features.books.dto.CreateBookBody;
import dev.ymkz.demo.api.features.books.dto.FindBookByIdResponse;
import dev.ymkz.demo.api.features.books.dto.SearchBooksQueryParam;
import dev.ymkz.demo.api.features.books.dto.SearchBooksResponse;
import dev.ymkz.demo.api.features.books.dto.UpdateBookBody;
import dev.ymkz.demo.api.shared.exception.ProblemDetailResponse;
import dev.ymkz.demo.api.shared.logging.EventLogContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@Tag(name = "Book")
@RequiredArgsConstructor
public class BookController {

    private final BookSearchUsecase bookSearchUsecase;
    private final BookDownloadUsecase bookDownloadUsecase;
    private final BookFindUsecase bookFindUsecase;
    private final BookCreateUsecase bookCreateUsecase;
    private final BookUpdateUsecase bookUpdateUsecase;
    private final BookDeleteUsecase bookDeleteUsecase;
    private final EventLogContext eventLog;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Operation(operationId = "searchBooks", description = "書籍情報を検索して取得する")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功"),
                @ApiResponse(
                        responseCode = "400",
                        description = "不正なリクエスト",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "アプリケーションエラー",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
            })
    public SearchBooksResponse searchBooks(
            @Valid @ParameterObject SearchBooksQueryParam queryParam,
            @Parameter(description = "取得位置") @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @Parameter(description = "取得数") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        var data = bookSearchUsecase.execute(queryParam.toBookSearchQuery(offset, limit));

        eventLog.set("book.search.total_results", data.totalCount());
        eventLog.addEvent(
                "book_search_executed",
                "isbn",
                queryParam.isbn(),
                "title",
                queryParam.title(),
                "offset",
                offset,
                "limit",
                limit,
                "total_results",
                data.totalCount());

        return SearchBooksResponse.of(data);
    }

    @GetMapping(path = "download", produces = "text/csv")
    @Operation(operationId = "downloadBooks", description = "[Internal Use Only]書籍情報をCSV形式でダウンロードする")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功"),
                @ApiResponse(
                        responseCode = "400",
                        description = "不正なリクエスト",
                        content =
                                @Content(
                                        schema = @Schema(implementation = ProblemDetailResponse.class),
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE)),
                @ApiResponse(
                        responseCode = "500",
                        description = "アプリケーションエラー",
                        content =
                                @Content(
                                        schema = @Schema(implementation = ProblemDetailResponse.class),
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE)),
            })
    public ResponseEntity<byte[]> downloadBooks(@ParameterObject SearchBooksQueryParam queryParam) {
        var data = bookDownloadUsecase.execute(queryParam.toBookSearchQuery(null, null));

        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=download.csv")
                .contentType(new MediaType("text", "csv"))
                .body(data);
    }

    @GetMapping(path = "{id}", produces = APPLICATION_JSON_VALUE)
    @Operation(operationId = "findBookById", description = "指定したIDの書籍情報を取得する")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功"),
                @ApiResponse(
                        responseCode = "400",
                        description = "不正なリクエスト",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "存在しないリソース",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "アプリケーションエラー",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
            })
    public FindBookByIdResponse findBookById(@PathVariable long id) {
        return FindBookByIdResponse.of(bookFindUsecase.execute(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createBook", description = "新しい書籍情報を作成する")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "成功"),
                @ApiResponse(
                        responseCode = "400",
                        description = "不正なリクエスト",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "アプリケーションエラー",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
            })
    public void createBook(@Validated @RequestBody CreateBookBody body) {
        bookCreateUsecase.execute(body.toCommand());
    }

    @PatchMapping(path = "{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateBook", description = "指定したIDの書籍情報を更新する")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "成功"),
                @ApiResponse(
                        responseCode = "400",
                        description = "不正なリクエスト",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "存在しないリソース",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "アプリケーションエラー",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
            })
    public void updateBook(@PathVariable long id, @Validated @RequestBody UpdateBookBody body) {
        bookUpdateUsecase.execute(body.toCommand(id));
    }

    @DeleteMapping(path = "{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteBook", description = "指定したIDの書籍情報を削除する")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "成功"),
                @ApiResponse(
                        responseCode = "400",
                        description = "不正なリクエスト",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "存在しないリソース",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "アプリケーションエラー",
                        content =
                                @Content(
                                        mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetailResponse.class))),
            })
    public void deleteBook(@PathVariable long id) {
        bookDeleteUsecase.execute(id);
    }
}
