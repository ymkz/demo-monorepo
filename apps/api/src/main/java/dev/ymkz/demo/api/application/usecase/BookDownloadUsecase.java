package dev.ymkz.demo.api.application.usecase;

import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.repository.BookRepository;
import dev.ymkz.demo.api.infrastructure.logging.EventsCollector;
import dev.ymkz.demo.api.presentation.dto.DownloadBooksResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    public byte[] execute(BookSearchQuery query) {
        var dbStart = System.currentTimeMillis();
        var books = repository.download(query);
        EventsCollector.record(
                "db_query", "book_download", System.currentTimeMillis() - dbStart, new DbMetadata(books.size()));

        var text = mapBooksToCsvText(
                books.stream().map(DownloadBooksResponse::from).toList());
        var bytes = text.getBytes(StandardCharsets.UTF_8);
        return bytes;
    }

    private record DbMetadata(int rowCount) {}

    private String mapBooksToCsvText(List<DownloadBooksResponse> data) {
        var start = System.currentTimeMillis();
        try {
            var schema = mapper.schemaFor(DownloadBooksResponse.class).withHeader();
            var text = mapper.writer(schema).writeValueAsString(data);
            var textWithBom = new String(UTF8_BOM) + text;

            EventsCollector.record(
                    "data_conversion",
                    "csv_generation",
                    System.currentTimeMillis() - start,
                    new CsvMetadata(data.size()));

            return textWithBom;
        } catch (JacksonException ex) {
            String requestId = EventsCollector.getRequestId();
            log.error("Failed to convert to CSV requestId={}", requestId, ex);
            EventsCollector.setError("data_conversion", "csv_generation", ex, new CsvMetadata(data.size()));
            throw new RuntimeException("Failed to convert to CSV", ex);
        }
    }

    private record CsvMetadata(int rowCount) {}
}
