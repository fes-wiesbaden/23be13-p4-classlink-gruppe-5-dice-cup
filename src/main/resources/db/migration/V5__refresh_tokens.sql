create table refresh_token
(
    id         uuid primary key,
    user_id    uuid        not null references users (id) on delete cascade,
    token_hash bytea       not null,
    token_salt bytea       not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    reused_at  timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index ix_refresh_token_user_id on refresh_token (user_id);

