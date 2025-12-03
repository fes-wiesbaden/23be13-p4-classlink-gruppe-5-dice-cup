CREATE TABLE refresh_token
(
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    token_hash BYTEA                       NOT NULL,
    token_salt BYTEA                       NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITHOUT TIME ZONE,
    reused_at  TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id)
);

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_on_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX ix_refresh_token_user ON refresh_token (user_id);
