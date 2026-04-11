package dev.ymkz.demo.api.features.books.application;

import dev.ymkz.demo.api.features.books.domain.BookRepository;
import dev.ymkz.demo.api.features.books.domain.BookSearchQuery;
import dev.ymkz.demo.api.features.books.presentation.dto.DownloadBooksResponse;
import dev.ymkz.demo.api.shared.logging.EventsCollector;
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
        var books = repository.download(query);
        EventsCollector.addEvent("book_download_executed", new DbMetadata(books.size()));

        return mapBooksToCsvText(books.stream().map(DownloadBooksResponse::from).toList())
                .getBytes(StandardCharsets.UTF_8);
    }

    private record DbMetadata(int rowCount) {}

    private String mapBooksToCsvText(List<DownloadBooksResponse> data) {
        try {
            var schema = mapper.schemaFor(DownloadBooksResponse.class).withHeader();
            var text = mapper.writer(schema).writeValueAsString(data);
            var textWithBom = new String(UTF8_BOM) + text;

            EventsCollector.addEvent("csv_generation_executed", new CsvMetadata(data.size()));

            return textWithBom;
        } catch (JacksonException ex) {
            String requestId = EventsCollector.getRequestId();
            log.error("Failed to convert to CSV requestId={}", requestId, ex);
            EventsCollector.setError(ex, new CsvMetadata(data.size()));
            throw new RuntimeException("Failed to convert to CSV", ex);
        }
    }

    private record CsvMetadata(int rowCount) {}
}
