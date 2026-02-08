package dev.ymkz.demo.core.domain.valueobject;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record Paginated<T>(
        List<T> items,
        @PositiveOrZero int totalCount,
        @Min(0) int offset,
        @Min(1) @Max(100) int limit) {

    public int returnedCount() {
        return items.size();
    }

    public int currentPage() {
        return (int) (totalCount > 0 ? Math.floor(offset / limit) + 1 : 1);
    }

    public int totalPages() {
        return (int) (totalCount > 0 ? Math.ceil(totalCount / limit) : 1);
    }
}
