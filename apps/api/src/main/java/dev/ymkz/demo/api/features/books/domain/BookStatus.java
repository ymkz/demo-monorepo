package dev.ymkz.demo.api.features.books.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BookStatus {
    UNPUBLISHED,
    PUBLISHED,
    OUT_OF_PRINT,
}
