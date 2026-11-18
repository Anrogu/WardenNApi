CREATE TABLE IF NOT EXISTS api_keys (
                                        id           UUID PRIMARY KEY,
                                        name         VARCHAR(100) NOT NULL,
    owner        VARCHAR(100) NOT NULL,
    key_prefix   VARCHAR(20)  NOT NULL,
    key_hash     CHAR(64)     NOT NULL UNIQUE,
    scopes       TEXT[]       NOT NULL DEFAULT '{}',
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    expires_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_api_keys_owner ON api_keys (owner);