package com.github.weierstrass54.component.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

/**
 * Обработка ошибки ограчниения доступа к ресурсу.
 */
@Component
public class RestAccessDeniedHandler extends SecurityErrorHandler<AccessDeniedException> implements ServerAccessDeniedHandler {
    @Autowired
    public RestAccessDeniedHandler(ExchangeStrategies exchangeStrategies, ErrorMessageConverter errorMessageConverter) {
        super(HttpStatus.FORBIDDEN, exchangeStrategies, errorMessageConverter);
    }
}
