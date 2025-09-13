package dev.ymkz.demo.api.domain.model;

import dev.ymkz.demo.core.domain.valueobject.Isbn;
import dev.ymkz.demo.core.domain.valueobject.RangeInteger;
import dev.ymkz.demo.core.domain.valueobject.RangeTime;
import jakarta.annotation.Nullable;
import java.util.List;

public record BookSearchQuery(
        @Nullable Isbn isbn,
        @Nullable String title,
        @Nullable RangeInteger priceRange,
        @Nullable List<BookStatus> statuses,
        @Nullable RangeTime publishedAtRange,
        @Nullable BookOrder order,
        @Nullable Integer offset,
        @Nullable Integer limit) {}
