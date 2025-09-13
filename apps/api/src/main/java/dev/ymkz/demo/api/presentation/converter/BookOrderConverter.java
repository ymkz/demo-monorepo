package dev.ymkz.demo.api.presentation.converter;

import dev.ymkz.demo.api.domain.model.BookOrder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BookOrderConverter implements Converter<String, BookOrder> {

    @Override
    public BookOrder convert(@NonNull String source) {
        return BookOrder.fromString(source);
    }
}
