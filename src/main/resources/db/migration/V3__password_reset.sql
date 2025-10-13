CREATE TABLE password_reset
(
    id           uuid PRIMARY KEY,
    user_id      uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash   bytea       NOT NULL,
    token_salt   bytea       NOT NULL,
    status       text        NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REDEEMED', 'REVOKED', 'EXPIRED')),
    expires_at   timestamptz NOT NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    used_at      timestamptz,
    note         text,
    public_token text
);

CREATE INDEX ix_password_reset_token_hash ON password_reset (token_hash);
CREATE INDEX ix_password_reset_expires_at ON password_reset (expires_at);
