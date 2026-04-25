package dev.ymkz.demo.api.features.books.presentation;

import dev.ymkz.demo.api.features.books.domain.BookOrder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BookOrderConverter implements Converter<String, BookOrder> {

    @Override
    public BookOrder convert(String source) {
        return BookOrder.fromString(source);
    }
}
