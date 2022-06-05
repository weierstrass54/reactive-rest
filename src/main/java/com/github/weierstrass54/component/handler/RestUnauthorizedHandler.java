package com.github.weierstrass54.component.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Обработка ошибки неавторизованного доступа.
 */
@Component
public class RestUnauthorizedHandler extends SecurityErrorHandler<AuthenticationException> implements ServerAuthenticationEntryPoint {
    @Autowired
    public RestUnauthorizedHandler(ExchangeStrategies exchangeStrategies, ErrorMessageConverter errorMessageConverter) {
        super(HttpStatus.UNAUTHORIZED, exchangeStrategies, errorMessageConverter);
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return handle(exchange, ex);
    }
}
