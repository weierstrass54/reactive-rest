package com.github.weierstrass54.component.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.weierstrass54.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    private final JWTVerifier jwtVerifier;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationManager(Algorithm algorithm, ObjectMapper objectMapper) {
        jwtVerifier = JWT.require(algorithm)
            .withIssuer("rest-reactive")
            .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
            .map(auth -> jwtVerifier.verify((String) auth.getCredentials()))
            .map(jwt -> jwt.getClaim("authenticated"))
            .flatMap(claim -> parseUser(claim)
                .map(user -> new UsernamePasswordAuthenticationToken(user, claim.asString(), user.getAuthorities()))
            )
            .map(token -> (Authentication) token)
            .onErrorResume(t -> {
                log.error("Ошибка аутентификации: {}", t.getMessage(), t);
                return Mono.empty();
            });
    }

    private Mono<User> parseUser(Claim claim) {
        try {
            return Mono.just(objectMapper.readValue(claim.asString(), User.class));
        }
        catch (IOException e) {
            return Mono.error(e);
        }
    }
}
