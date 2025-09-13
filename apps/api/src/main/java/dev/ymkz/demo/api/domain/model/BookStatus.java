package dev.ymkz.demo.api.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BookStatus {
    UNPUBLISHED,
    PUBLISHED,
    OUT_OF_PRINT,
}
