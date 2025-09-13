package dev.ymkz.demo.core.domain.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.ymkz.demo.core.domain.valueobject.Isbn;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IsbnTest {

    @Test
    void nullIsbnShouldReturnNull() {
        Isbn isbn = Isbn.of(null);
        assertNull(isbn);
    }

    @ParameterizedTest
    @MethodSource("validIsbnProvider")
    void validIsbnShouldBeCreated(String isbnValue) {
        Isbn isbn = Isbn.of(isbnValue);
        assertNotNull(isbn);
        assertEquals(isbnValue, isbn.value());
    }

    static Stream<Arguments> validIsbnProvider() {
        return Stream.of(Arguments.of("9784150310196"), Arguments.of("9784150117429"));
    }

    @ParameterizedTest
    @MethodSource("invalidIsbnProvider")
    void invalidIsbnShouldThrowException(String isbnValue) {
        assertThrows(IllegalArgumentException.class, () -> Isbn.of(isbnValue));
    }

    static Stream<Arguments> invalidIsbnProvider() {
        return Stream.of(
                Arguments.of("123456789012"),
                Arguments.of("9781234567890"),
                Arguments.of("invalidisbn123"),
                Arguments.of("97812345678a7"));
    }
}
