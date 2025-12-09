ALTER TABLE project
    ADD COLUMN term_id                UUID,
    ADD COLUMN responsible_teacher_id UUID,
    ADD COLUMN updated_at             TIMESTAMP WITHOUT TIME ZONE;

-- reuse the same timestamp when upgrading legacy rows
UPDATE project
SET updated_at = created_at
WHERE updated_at IS NULL;

-- ensure every project references a term (legacy data falls back to a placeholder term)
INSERT INTO terms (id, name, status, start_date, end_date, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001',
        'Legacy Term',
        'OPEN',
        CURRENT_DATE,
        CURRENT_DATE,
        NOW(),
        NOW())
ON CONFLICT (id) DO NOTHING;

UPDATE project
SET term_id = '00000000-0000-0000-0000-000000000001'
WHERE term_id IS NULL;

UPDATE project
SET name = 'Legacy Project'
WHERE name IS NULL;

ALTER TABLE project
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE project
    ALTER COLUMN term_id SET NOT NULL;

ALTER TABLE project
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE project
    ADD CONSTRAINT fk_project_term FOREIGN KEY (term_id) REFERENCES terms (id);

ALTER TABLE project
    ADD CONSTRAINT fk_project_responsible_teacher FOREIGN KEY (responsible_teacher_id) REFERENCES teacher (user_id);
