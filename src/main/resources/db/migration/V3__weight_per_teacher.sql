ALTER TABLE grades
    DROP CONSTRAINT fk_grades_on_teacher;

ALTER TABLE project_subject
    DROP CONSTRAINT fk_project_subject_on_project;

ALTER TABLE password_reset
    DROP CONSTRAINT password_reset_user_id_fkey;

CREATE TABLE teacher_field
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by UUID,
    updated_by UUID,
    project_id UUID NOT NULL,
    teacher_id UUID NOT NULL,
    weight     DECIMAL(10, 8),
    CONSTRAINT pk_teacher_field PRIMARY KEY (id)
);

ALTER TABLE project_subject
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE project_subject
    ADD created_by UUID;

ALTER TABLE project_subject
    ADD teacher_field_id UUID;

ALTER TABLE project_subject
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE project_subject
    ADD updated_by UUID;

ALTER TABLE project_subject
    ALTER COLUMN teacher_field_id SET NOT NULL;

ALTER TABLE project
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE project
    ADD updated_by UUID;

ALTER TABLE teacher_field
    ADD CONSTRAINT uc_36f34e2639846bee13d9ebf98 UNIQUE (project_id);

ALTER TABLE project_subject
    ADD CONSTRAINT uc_372cb6cf483e9ab2964b9a3d9 UNIQUE (teacher_field_id, subject_id);

ALTER TABLE project_subject
    ADD CONSTRAINT FK_PROJECT_SUBJECT_ON_TEACHER_FIELD FOREIGN KEY (teacher_field_id) REFERENCES teacher_field (id);

ALTER TABLE refresh_token
    ADD CONSTRAINT FK_REFRESH_TOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE teacher_field
    ADD CONSTRAINT FK_TEACHER_FIELD_ON_PROJECT FOREIGN KEY (project_id) REFERENCES project (id);

ALTER TABLE project_subject
    DROP COLUMN project_id;

ALTER TABLE project_subject
    DROP COLUMN id;

ALTER TABLE grades
    DROP COLUMN teacher_id;

ALTER TABLE grades
    DROP COLUMN grade_value;

ALTER TABLE grades
    DROP COLUMN project_subject_id;

DROP SEQUENCE project_subject_seq CASCADE;

ALTER TABLE project
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE grades
    ADD grade_value DECIMAL(10, 8) NOT NULL;

ALTER TABLE project_subject
    ADD id UUID NOT NULL PRIMARY KEY;

ALTER TABLE project_subject
    ADD CONSTRAINT uc_372cb6cf483e9ab2964b9a3d9 UNIQUE (id);

ALTER TABLE grades
    ADD project_subject_id UUID NOT NULL;

ALTER TABLE grades
    ADD CONSTRAINT ux_grade_once_per_student_in_project_subject UNIQUE (project_subject_id);

ALTER TABLE grades
    ADD CONSTRAINT FK_GRADES_ON_PROJECT_SUBJECT FOREIGN KEY (project_subject_id) REFERENCES project_subject (id);

ALTER TABLE project_subject
    ALTER COLUMN weight TYPE DECIMAL(10, 8) USING (weight::DECIMAL(10, 8));