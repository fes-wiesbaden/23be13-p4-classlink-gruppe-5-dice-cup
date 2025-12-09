CREATE TABLE school_years
(
    id         UUID                        NOT NULL,
    name       VARCHAR(100)                NOT NULL,
    start_date DATE                        NOT NULL,
    end_date   DATE                        NOT NULL,
    status     VARCHAR(10)                 NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_school_years PRIMARY KEY (id)
);

INSERT INTO school_years (id, name, start_date, end_date, status, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000010', 'Legacy School Year', CURRENT_DATE, CURRENT_DATE, 'ACTIVE', NOW(),
        NOW())
ON CONFLICT (id) DO NOTHING;

ALTER TABLE terms
    ADD COLUMN sequence_number INTEGER;

ALTER TABLE terms
    ADD COLUMN school_year_id UUID;

UPDATE terms
SET school_year_id = '00000000-0000-0000-0000-000000000010'
WHERE school_year_id IS NULL;

ALTER TABLE terms
    ALTER COLUMN school_year_id SET NOT NULL;

ALTER TABLE terms
    ADD CONSTRAINT fk_terms_school_year FOREIGN KEY (school_year_id) REFERENCES school_years (id);
