package com.github.weierstrass54.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PageRequest {
    private final int page;
    private final int size;
    private final Direction direction;

    public static PageRequest of(Integer page, Integer size, Direction direction) {
        return new PageRequest(
            Optional.ofNullable(page).filter(p -> p > 0).orElse(1),
            Optional.ofNullable(size).filter(s -> s > 0).orElse(25),
            Optional.ofNullable(direction).orElse(Direction.ASC)
        );
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    public enum Direction {
        ASC, DESC;

        public static Direction of(String value) {
            return Arrays.stream(values())
                .filter(d -> d.name().equalsIgnoreCase(value))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Сортировка допускает только 'ACS' и 'DESC' значения"));
        }
    }
}
