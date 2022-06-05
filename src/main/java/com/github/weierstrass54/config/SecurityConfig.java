package com.github.weierstrass54.config;

import com.auth0.jwt.algorithms.Algorithm;
import com.github.weierstrass54.component.handler.RestAccessDeniedHandler;
import com.github.weierstrass54.component.handler.RestUnauthorizedHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public Algorithm algorithm() {
        return Algorithm.HMAC512("secret");
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter(
        ServerAuthenticationConverter serverAuthenticationConverter,
        ReactiveAuthenticationManager reactiveAuthenticationManager
    ) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(serverAuthenticationConverter);
        return authenticationWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http,
        AuthenticationWebFilter authenticationWebFilter,
        RestAccessDeniedHandler restAccessDeniedHandler,
        RestUnauthorizedHandler restUnauthorizedHandler
    ) {
        return http
            .httpBasic().disable()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .exceptionHandling()
                .accessDeniedHandler(restAccessDeniedHandler)
                .authenticationEntryPoint(restUnauthorizedHandler)
            .and()
            .authorizeExchange()
                .pathMatchers("/", "/test", "/auth/**", "/v2/api-docs", "/swagger-ui/**", "/swagger-resources/**", "/actuator/**").permitAll()
                .anyExchange().authenticated()
            .and()
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
