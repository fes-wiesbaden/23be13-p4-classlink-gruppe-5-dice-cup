CREATE TABLE project_groups
(
    id                     UUID                        NOT NULL,
    project_id             UUID                        NOT NULL,
    group_number           INTEGER                     NOT NULL,
    supervising_teacher_id UUID,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_project_groups PRIMARY KEY (id),
    CONSTRAINT uk_project_groups_project_number UNIQUE (project_id, group_number)
);

ALTER TABLE project_groups
    ADD CONSTRAINT fk_project_groups_project FOREIGN KEY (project_id) REFERENCES project (id);

ALTER TABLE project_groups
    ADD CONSTRAINT fk_project_groups_teacher FOREIGN KEY (supervising_teacher_id) REFERENCES teacher (user_id);

CREATE TABLE project_group_members
(
    id         UUID                        NOT NULL,
    group_id   UUID                        NOT NULL,
    student_id UUID                        NOT NULL,
    role       VARCHAR(20),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_project_group_members PRIMARY KEY (id),
    CONSTRAINT uk_project_group_members_group_student UNIQUE (group_id, student_id)
);

ALTER TABLE project_group_members
    ADD CONSTRAINT fk_project_group_members_group FOREIGN KEY (group_id) REFERENCES project_groups (id);

ALTER TABLE project_group_members
    ADD CONSTRAINT fk_project_group_members_student FOREIGN KEY (student_id) REFERENCES students (id);
