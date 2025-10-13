CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE registration_invites
    ALTER COLUMN email TYPE citext USING email::citext;

UPDATE registration_invites
SET role = upper(role);

ALTER TABLE registration_invites
    ALTER COLUMN role TYPE text,
    ADD CONSTRAINT ck_registration_invites_role CHECK (role IN ('ADMIN', 'LEHRER', 'SCHUELER'));

ALTER TABLE registration_invites
    ALTER COLUMN expires_at TYPE timestamptz USING expires_at AT TIME ZONE 'UTC';

ALTER TABLE registration_invites
    ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

ALTER TABLE registration_invites
    ALTER COLUMN used_at TYPE timestamptz USING used_at AT TIME ZONE 'UTC';

ALTER TABLE registration_invites
    ALTER COLUMN token_hash TYPE bytea USING decode(token_hash, 'hex');

ALTER TABLE registration_invites
    ALTER COLUMN created_by SET DEFAULT gen_random_uuid();

UPDATE registration_invites
SET created_by = gen_random_uuid()
WHERE created_by IS NULL;

ALTER TABLE registration_invites
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE registration_invites
    ADD COLUMN IF NOT EXISTS token_salt bytea;

UPDATE registration_invites
SET token_salt = gen_random_bytes(16)
WHERE token_salt IS NULL;

ALTER TABLE registration_invites
    ALTER COLUMN token_salt SET NOT NULL;

ALTER TABLE registration_invites
    ADD COLUMN IF NOT EXISTS class_id     uuid,
    ADD COLUMN IF NOT EXISTS public_token text,
    ADD COLUMN IF NOT EXISTS status       text        NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS uses_count   integer     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS max_uses     integer     NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS updated_at   timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS note         text;

ALTER TABLE registration_invites
    ADD CONSTRAINT ck_registration_invites_status CHECK (status IN ('PENDING', 'REDEEMED', 'REVOKED', 'EXPIRED'));

CREATE INDEX IF NOT EXISTS ix_registration_invites_token_hash ON registration_invites (token_hash);
CREATE INDEX IF NOT EXISTS ix_registration_invites_expires_at ON registration_invites (expires_at);
CREATE INDEX IF NOT EXISTS ix_registration_invites_email_status ON registration_invites (email, status);

CREATE OR REPLACE FUNCTION set_registration_invite_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_registration_invite_updated_at ON registration_invites;
CREATE TRIGGER trg_registration_invite_updated_at
    BEFORE UPDATE
    ON registration_invites
    FOR EACH ROW
EXECUTE FUNCTION set_registration_invite_updated_at();
