package dev.ymkz.demo.core.domain.valueobject;

import java.time.Instant;

public record RangeTime(Instant start, Instant end) {

    public RangeTime {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("The start value is greater than the end value");
        }
    }

    public static RangeTime of(Instant start, Instant end) {
        return new RangeTime(start != null ? start : null, end != null ? end : null);
    }
}
