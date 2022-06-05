package com.github.weierstrass54.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Getter
public enum Authority implements GrantedAuthority {
    INTERNAL,
    ADMIN,
    OPERATOR,
    CRM,
    EXAMINEE;

    @Override
    public String getAuthority() {
        return name();
    }

    public static Mono<Authority> of(String value) {
        return Arrays.stream(values())
            .filter(r -> r.name().equals(value.toUpperCase()))
            .findAny()
            .map(Mono::just)
            .orElse(Mono.error(() -> new IllegalArgumentException("Роли " + value + " не существует.")));
    }
}
