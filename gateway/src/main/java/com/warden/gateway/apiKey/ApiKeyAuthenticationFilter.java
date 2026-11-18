package com.warden.gateway.apiKey;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ApiKeyAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String API_KEY_HEADER = "X-API-Key";
    private static final String PROTECTED_PREFIX = "/api/partner";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!isProtected(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        String presentedKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (presentedKey == null || presentedKey.isBlank()) {
            return reject(exchange, "missing_api_key");
        }

        return apiKeyService.authenticate(presentedKey)
                .map(apiKey -> chain.filter(withIdentity(exchange, apiKey)))
                .defaultIfEmpty(Mono.empty())
                .flatMap(result -> result)
                .switchIfEmpty(Mono.defer(() -> reject(exchange, "invalid_api_key")));
    }

    /**
     * Coincide con "/api/partner" y "/api/partner/**". Sin la barra final en el prefijo,
     * una petición a "/api/partner" se colaría sin credencial.
     */
    private boolean isProtected(String path) {
        return path.equals(PROTECTED_PREFIX) || path.startsWith(PROTECTED_PREFIX + "/");
    }

    private ServerWebExchange withIdentity(ServerWebExchange exchange, ApiKey apiKey) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(API_KEY_HEADER);
                    headers.set("X-Warden-Key-Id", apiKey.id().toString());
                    headers.set("X-Warden-Owner", apiKey.owner());
                    headers.set("X-Warden-Scopes", String.join(",", apiKey.scopes()));
                })
                .build();

        return exchange.mutate().request(request).build();
    }

    private Mono<Void> reject(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("WWW-Authenticate", "ApiKey");

        byte[] body = ("{\"error\":\"" + reason + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}