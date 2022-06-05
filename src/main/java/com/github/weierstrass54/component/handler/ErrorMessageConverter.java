package com.github.weierstrass54.component.handler;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Коневертация исключения в ErrorMessage.
 */
@Component
public class ErrorMessageConverter {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Mono<ErrorMessage> convert(ServerHttpRequest request, Throwable t) {
        return now().map(now -> {
            String id = request.getId();
            String method = request.getMethodValue();
            String query = request.getURI() + queryParams(request.getQueryParams());
            if (t instanceof MethodArgumentNotValidException manve) {
                String message = manve.getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
                return new ErrorMessage(now, id, method, query, message);
            }
            if (t instanceof NullPointerException) {
                return new ErrorMessage(now, id, method, query, "NPE.");
            }
            return new ErrorMessage(now, id, method, query, t.getMessage());
        });
    }

    private String queryParams(MultiValueMap<String, String> params) {
        String result = params.entrySet().stream().map(e -> {
            String key = e.getKey() + (!e.getValue().isEmpty() ? "[]" : "");
            return e.getValue().stream().map(value -> key + "=" + value).collect(Collectors.joining("&"));
        }).collect(Collectors.joining("&"));
        return result.isBlank() ? result : "?" + result;
    }

    private Mono<String> now() {
        return Mono.defer(() -> Mono.just(LocalDateTime.now())).map(DTF::format);
    }
}
