package com.github.weierstrass54.component.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Обработка ошибок безопасности (аутентификации и авторизации).
 * @param <T> исключение, возникшее при проверке безопасности
 */
@Slf4j
public abstract class SecurityErrorHandler<T extends RuntimeException> {
    private final static ResolvableType ERROR_TYPE = ResolvableType.forClass(ErrorMessage.class);
    private final HttpStatus httpStatus;
    private final ExchangeStrategies exchangeStrategies;
    private final ErrorMessageConverter errorMessageConverter;
    private final Mono<ErrorWriter> defaultErrorWriter;

    @Autowired
    public SecurityErrorHandler(HttpStatus httpStatus, ExchangeStrategies exchangeStrategies, ErrorMessageConverter converter) {
        this.httpStatus = httpStatus;
        this.exchangeStrategies = exchangeStrategies;
        this.errorMessageConverter = converter;
        // дефолтный writer сообщения на случай, если запрос потребует неподдерживаемый content-type
        this.defaultErrorWriter = exchangeStrategies.messageWriters().stream()
            .filter(w -> w.canWrite(ERROR_TYPE, MediaType.APPLICATION_JSON))
            .findAny()
            .map(writer -> new ErrorWriter(MediaType.APPLICATION_JSON, writer))
            .map(Mono::just)
            .orElseGet(() -> {
                log.error("Cannot find appropriate http body writer for default media type {}", MediaType.APPLICATION_JSON);
                return Mono.empty();
            });
    }

    public final Mono<Void> handle(ServerWebExchange exchange, T ex) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // writer требует Publisher<?> для отрисовки, поэтому не используем цепочку Mono-преобразований
        Mono<ErrorMessage> errorMessage = errorMessageConverter.convert(request, ex);
        return getMessageWriter(request.getHeaders().getContentType())
            .flatMap(errorWriter -> Mono.defer(() -> {  // Mono.defer обязателен, так как рендер ошибки является ленивой процедурой
                response.setStatusCode(httpStatus);
                response.getHeaders().setContentType(errorWriter.getMediaType());
                return errorWriter.getWriter().write(
                    errorMessage, ERROR_TYPE, ERROR_TYPE, errorWriter.getMediaType(), request, response,
                        Collections.emptyMap()
                );
            }
        ));
    }

    /**
     * Получение пары content-type, writer для рендера ответа с ошибкой с учётом content-type запроса.
     * Если сервис не поддерживает запрошенный content-type, то возвращается дефолтная пара.
     * @param mediaType content-type запроса
     * @return подходящая пара content-type, writer для рендера ответа
     */
    private Mono<ErrorWriter> getMessageWriter(MediaType mediaType) {
        return exchangeStrategies.messageWriters().stream()
            .filter(w -> w.canWrite(ERROR_TYPE, mediaType))
            .findAny()
            .map(writer -> new ErrorWriter(mediaType, writer))
            .map(Mono::just)
            .orElseGet(() -> {
                log.error("Cannot find appropriate http body writer for media type {}, using default.", mediaType);
                return defaultErrorWriter;
            });
    }

    @Getter
    private static final class ErrorWriter {
        private final MediaType mediaType;
        private final HttpMessageWriter<ErrorMessage> writer;

        @SuppressWarnings("unchecked")
        public ErrorWriter(MediaType mediaType, HttpMessageWriter<?> writer) {
            this.mediaType = mediaType;
            this.writer = (HttpMessageWriter<ErrorMessage>) writer;
        }
    }
}
