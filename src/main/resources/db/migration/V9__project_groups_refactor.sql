DROP TABLE IF EXISTS project_group_members;
DROP TABLE IF EXISTS project_groups;

ALTER TABLE student_groups
    RENAME TO project_groups;

ALTER TABLE project_groups
    RENAME CONSTRAINT pk_student_groups TO pk_project_groups;
ALTER TABLE project_groups
    RENAME CONSTRAINT ux_student_groups_project_groupno TO uk_project_group_number;
ALTER TABLE project_groups
    DROP CONSTRAINT IF EXISTS fk_student_groups_on_project;
ALTER TABLE project_groups
    DROP CONSTRAINT IF EXISTS "FK_STUDENT_GROUPS_ON_PROJECT";
ALTER TABLE project_groups
    ADD CONSTRAINT fk_project_groups_project FOREIGN KEY (project_id) REFERENCES project (id);

ALTER TABLE project_groups
    ADD COLUMN supervising_teacher_id UUID,
    ADD COLUMN created_at             TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN updated_at             TIMESTAMP WITHOUT TIME ZONE;

UPDATE project_groups
SET created_at = NOW(),
    updated_at = NOW()
WHERE created_at IS NULL
   OR updated_at IS NULL;

ALTER TABLE project_groups
    ADD CONSTRAINT fk_project_groups_teacher FOREIGN KEY (supervising_teacher_id) REFERENCES teacher (user_id);

ALTER TABLE group_members
    DROP CONSTRAINT IF EXISTS fk_group_members_on_group;
ALTER TABLE group_members
    DROP CONSTRAINT IF EXISTS "FK_GROUP_MEMBERS_ON_GROUP";

ALTER TABLE group_members
    ADD COLUMN role       VARCHAR(20),
    ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE group_members
SET created_at = NOW(),
    updated_at = NOW()
WHERE created_at IS NULL
   OR updated_at IS NULL;

ALTER TABLE group_members
    ADD CONSTRAINT fk_group_members_on_group FOREIGN KEY (group_id) REFERENCES project_groups (id);

ALTER TABLE project_groups
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE group_members
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;
