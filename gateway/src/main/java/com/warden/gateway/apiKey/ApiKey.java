package com.warden.gateway.apiKey;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_keys")
public record ApiKey(
        @Id UUID id,
        String name,
        String owner,
        @Column("key_prefix") String keyPrefix,
        @Column("key_hash") String keyHash,
        String[] scopes,
        boolean enabled,
        @Column("expires_at") Instant expiresAt,
        @Column("created_at") Instant createdAt,
        @Column("last_used_at") Instant lastUsedAt) {

    public boolean isUsable() {
        return enabled && (expiresAt == null || expiresAt.isAfter(Instant.now()));
    }
}