package com.warden.gateway.security;

import java.util.List;
import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class TrustedHeaderSanitizer implements WebFilter, Ordered {

    private static final String TRUSTED_PREFIX = "x-warden-";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        List<String> spoofed = request.getHeaders().keySet().stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(TRUSTED_PREFIX))
                .toList();

        if (spoofed.isEmpty()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest sanitized = request.mutate()
                .headers(headers -> spoofed.forEach(headers::remove))
                .build();

        return chain.filter(exchange.mutate().request(sanitized).build());
    }
}