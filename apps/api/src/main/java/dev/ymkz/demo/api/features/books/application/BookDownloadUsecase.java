package dev.ymkz.demo.api.features.books.application;

import dev.ymkz.demo.api.features.books.domain.BookRepository;
import dev.ymkz.demo.api.features.books.domain.BookSearchQuery;
import dev.ymkz.demo.api.features.books.presentation.dto.DownloadBooksResponse;
import dev.ymkz.demo.api.shared.logging.EventLogContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvWriteFeature;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookDownloadUsecase {

    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final CsvMapper mapper =
            CsvMapper.builder().enable(CsvWriteFeature.ALWAYS_QUOTE_STRINGS).build();

    private final BookRepository repository;
    private final EventLogContext eventLog;

    public byte[] execute(BookSearchQuery query) {
        var books = repository.download(query);
        eventLog.set("book.download.row_count", books.size());
        eventLog.addEvent("book_download_executed", "row_count", books.size());

        return mapBooksToCsvText(books.stream().map(DownloadBooksResponse::from).toList())
                .getBytes(StandardCharsets.UTF_8);
    }

    private String mapBooksToCsvText(List<DownloadBooksResponse> data) {
        try {
            var schema = mapper.schemaFor(DownloadBooksResponse.class).withHeader();
            var text = mapper.writer(schema).writeValueAsString(data);
            var textWithBom = new String(UTF8_BOM) + text;

            eventLog.set("csv.generation.row_count", data.size());
            eventLog.addEvent("csv_generation_executed", "row_count", data.size());

            return textWithBom;
        } catch (JacksonException ex) {
            log.error("Failed to convert to CSV", ex);
            eventLog.setError(ex, Map.of("row_count", data.size()));
            throw new RuntimeException("Failed to convert to CSV", ex);
        }
    }
}
