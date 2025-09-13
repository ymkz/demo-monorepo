package dev.ymkz.demo.api.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import dev.ymkz.demo.api.domain.model.BookSearchQuery;
import dev.ymkz.demo.api.domain.repository.BookRepository;
import dev.ymkz.demo.api.presentation.dto.DownloadBooksResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookDownloadUsecase {

    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final CsvMapper mapper = new CsvMapper().enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS);

    private final BookRepository repository;

    public byte[] execute(BookSearchQuery query) {
        var data = repository.search(query);
        var text = mapBooksToCsvText(
                data.items().stream().map(DownloadBooksResponse::from).toList());
        var bytes = text.getBytes(StandardCharsets.UTF_8);
        return bytes;
    }

    private String mapBooksToCsvText(List<DownloadBooksResponse> data) {
        try {
            var schema = mapper.schemaFor(DownloadBooksResponse.class).withHeader();
            var text = mapper.writer(schema).writeValueAsString(data);
            var textWithBom = new String(UTF8_BOM) + text;
            return textWithBom;
        } catch (JsonProcessingException ex) {
            log.error("Failed to convert to CSV: {}", ex);
            throw new RuntimeException("Failed to convert to CSV", ex);
        }
    }
}
