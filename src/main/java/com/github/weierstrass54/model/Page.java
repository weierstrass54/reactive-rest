package com.github.weierstrass54.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Page<T> {
    @Getter
    private final Collection<T> items;
    private final int size;

    @Getter
    private final long count;

    public static <T> Page<T> of(Collection<T> items, int size, long count) {
        return new Page<>(items, size, count);
    }

    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), 0, 0);
    }

    public long getTotalPages() {
        return count % size == 0 ? count / size : (count / size) + 1;
    }
}
