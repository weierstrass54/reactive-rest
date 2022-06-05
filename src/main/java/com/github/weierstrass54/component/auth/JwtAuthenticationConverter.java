package com.github.weierstrass54.component.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange)
            .flatMap(e -> Mono.justOrEmpty(e.getRequest().getHeaders().get(AUTH_HEADER)))
            .filter(headers -> !headers.isEmpty())
            .map(headers -> headers.get(0))
            .filter(bearer -> bearer.startsWith("Bearer "))
            .map(bearer -> bearer.substring(7))
            .map(bearer -> new UsernamePasswordAuthenticationToken(bearer, bearer));
    }
}
