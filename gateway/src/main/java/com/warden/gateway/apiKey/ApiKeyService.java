package com.warden.gateway.apiKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class ApiKeyService {

    private static final int MAX_KEY_LENGTH = 200;

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public Mono<ApiKey> authenticate(String presentedKey) {
        if (presentedKey == null || presentedKey.isBlank() || presentedKey.length() > MAX_KEY_LENGTH) {
            return Mono.empty();
        }

        return repository.findByKeyHash(sha256Hex(presentedKey))
                .filter(ApiKey::isUsable);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}