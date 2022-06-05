package com.github.weierstrass54.component.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorMessage {
    private final String now;
    private final String requestId;
    private final String method;
    private final String query;
    private final String message;

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "now='" + now + '\'' +
                ", requestId='" + requestId + '\'' +
                ", method='" + method + '\'' +
                ", query='" + query + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
