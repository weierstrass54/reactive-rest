package com.github.weierstrass54.component.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * Обрботка ошибок бизнес-логики.
 */
@Slf4j
@Order(-2)
@Component
@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {
    private final ErrorMessageConverter errorMessageConverter;

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorMessage> any(ServerHttpRequest request, Throwable t) {
        return errorMessageConverter.convert(request, t).doOnNext(em -> log.error("{}", em, t));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorMessage> badRequest(ServerHttpRequest request, Throwable t) {
        return errorMessageConverter.convert(request, t).doOnNext(em -> log.error("{}", em, t));
    }
}
