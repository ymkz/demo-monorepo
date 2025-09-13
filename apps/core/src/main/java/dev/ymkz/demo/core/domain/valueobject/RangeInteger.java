package dev.ymkz.demo.core.domain.valueobject;

public record RangeInteger(Integer min, Integer max) {

    public RangeInteger {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("The min value is greater than the max value");
        }
    }

    public static RangeInteger of(Integer min, Integer max) {
        return new RangeInteger(min, max);
    }
}
