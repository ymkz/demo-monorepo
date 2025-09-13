package dev.ymkz.demo.core.domain.valueobject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.regex.Pattern;

public record Isbn(@NotBlank @Size(min = 13, max = 13) String value) {

    private static final Pattern ISBN13_PATTERN = Pattern.compile("\\d{13}");

    public Isbn {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid ISBN-13 format");
        }
    }

    public static Isbn of(String value) {
        if (value == null) {
            return null;
        }
        return new Isbn(value);
    }

    private static boolean isValid(String value) {
        if (!ISBN13_PATTERN.matcher(value).matches()) {
            return false;
        }
        return isValidChecksum(value);
    }

    private static boolean isValidChecksum(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(isbn.charAt(i), 10);
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == Character.digit(isbn.charAt(12), 10);
    }
}
