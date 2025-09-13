package dev.ymkz.demo.core.domain.value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import dev.ymkz.demo.core.domain.valueobject.RangeInteger;
import org.junit.jupiter.api.Test;

class RangeIntegerTest {

    @Test
    void givenValidRange_whenCreatingRangeInteger_thenNoException() {
        // given
        Integer min = 1;
        Integer max = 10;

        // when
        RangeInteger range = RangeInteger.of(min, max);

        // then
        assertThat(range.min(), is(min));
        assertThat(range.max(), is(max));
    }

    @Test
    void givenInvalidRange_whenCreatingRangeInteger_thenThrowException() {
        // given
        Integer min = 10;
        Integer max = 1;

        // when
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> RangeInteger.of(min, max));

        // then
        assertThat(exception.getMessage(), is("The min value is greater than the max value"));
    }
}
