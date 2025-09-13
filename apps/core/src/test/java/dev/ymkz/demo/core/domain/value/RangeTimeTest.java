package dev.ymkz.demo.core.domain.value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import dev.ymkz.demo.core.domain.valueobject.RangeTime;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RangeTimeTest {

    @Test
    void givenValidRange_whenCreatingRangeTime_thenNoException() {
        // given
        var start = Instant.parse("2023-01-01T01:23:45.123Z");
        var end = Instant.parse("2023-01-02T01:23:45.123Z");

        // when
        RangeTime range = RangeTime.of(start, end);

        // then
        assertThat(range.start(), is(start));
        assertThat(range.end(), is(end));
    }

    @Test
    void givenInvalidRange_whenCreatingRangeTime_thenThrowException() {
        // given
        var start = Instant.parse("2023-01-02T01:23:45.123Z");
        var end = Instant.parse("2023-01-01T01:23:45.123Z");

        // when
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> RangeTime.of(start, end));

        // then
        assertThat(exception.getMessage(), is("The start value is greater than the end value"));
    }
}
