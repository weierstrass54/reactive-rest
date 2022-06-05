package com.github.weierstrass54.component.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Обработка ошибок маршрутизации.
 */
@Slf4j
@Order(-2)
@Component
public class RestErrorHandler extends DefaultErrorWebExceptionHandler {
    private final ErrorMessageConverter errorMessageConverter;
    private final ServerCodecConfigurer serverCodecConfigurer;

    @Autowired
    public RestErrorHandler(
        ErrorAttributes errorAttributes,
        WebProperties.Resources resources,
        ErrorProperties errorProperties,
        ApplicationContext applicationContext,
        ServerCodecConfigurer serverCodecConfigurer,
        ErrorMessageConverter errorMessageConverter
    ) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.errorMessageConverter = errorMessageConverter;
        this.serverCodecConfigurer = serverCodecConfigurer;
        setMessageReaders(this.serverCodecConfigurer.getReaders());
        setMessageWriters(this.serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderError);
    }

    private Mono<ServerResponse> renderError(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        return errorMessageConverter.convert(request.exchange().getRequest(), new RuntimeException(error.get("error").toString()))
            .doOnNext(errorMessage -> log.error("{}", errorMessage))
            .flatMap(errorMessage ->
                ServerResponse.status(getHttpStatus(error)).contentType(getContentType(request.exchange().getRequest()))
                    .body(BodyInserters.fromValue(errorMessage))
            );
    }

    /**
     * Получение content-type ответа с ошибкой в соответствии с запросом.
     * Если сервис не поддерживает запрошенный content-type, то выбирает дефолтный.
     * @param request запрос
     * @return подходящий content-type ответа
     */
    private MediaType getContentType(ServerHttpRequest request) {
        MediaType requiredMediaType = request.getHeaders().getContentType();
        return serverCodecConfigurer.getWriters().stream()
            .filter(w -> w.canWrite(ResolvableType.forClass(ErrorMessage.class), requiredMediaType))
            .findAny()
            .map(__ -> requiredMediaType)
            .orElseGet(() -> {
                log.warn("Cannot find appropriate http writer for media type {}, using default.", requiredMediaType);
                return MediaType.APPLICATION_JSON;
            });
    }
}
