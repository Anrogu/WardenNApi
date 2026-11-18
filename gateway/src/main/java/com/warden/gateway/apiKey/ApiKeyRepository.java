package com.warden.gateway.apiKey;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface ApiKeyRepository extends ReactiveCrudRepository<ApiKey, UUID> {

    Mono<ApiKey> findByKeyHash(String keyHash);
}