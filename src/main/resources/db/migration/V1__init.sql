CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE admin
(
    user_id UUID NOT NULL,
    CONSTRAINT pk_admin PRIMARY KEY (user_id)
);

CREATE TABLE assessment_questions
(
    id UUID NOT NULL,
    CONSTRAINT pk_assessment_questions PRIMARY KEY (id)
);

CREATE TABLE class_projects
(
    id         UUID    NOT NULL,
    class_id   UUID    NOT NULL,
    project_id UUID    NOT NULL,
    active     BOOLEAN NOT NULL,
    valid_from TIMESTAMP WITHOUT TIME ZONE,
    valid_to   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_class_projects PRIMARY KEY (id)
);

CREATE TABLE classes
(
    id   UUID         NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_classes PRIMARY KEY (id)
);

CREATE TABLE grades
(
    id                 UUID                        NOT NULL,
    project_subject_id UUID                        NOT NULL,
    student_id         UUID                        NOT NULL,
    teacher_id         UUID                        NOT NULL,
    grade_value        DOUBLE PRECISION            NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_grades PRIMARY KEY (id)
);

CREATE TABLE group_members
(
    id         UUID NOT NULL,
    student_id UUID NOT NULL,
    group_id   UUID NOT NULL,
    CONSTRAINT pk_group_members PRIMARY KEY (id)
);

CREATE TABLE project_subjects
(
    id         UUID          NOT NULL,
    project_id UUID          NOT NULL,
    subject_id UUID          NOT NULL,
    weight     DECIMAL(5, 2) NOT NULL,
    CONSTRAINT pk_project_subjects PRIMARY KEY (id)
);

CREATE TABLE projects
(
    id          UUID                        NOT NULL,
    class_id    UUID,
    name        VARCHAR(200)                NOT NULL,
    description VARCHAR(255),
    created_by  UUID,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    active      BOOLEAN                     NOT NULL,
    CONSTRAINT pk_projects PRIMARY KEY (id)
);

CREATE TABLE registration_invites
(
    id         UUID                        NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    role       VARCHAR(255)                NOT NULL,
    token_hash VARCHAR(255)                NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by UUID,
    used_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_registration_invites PRIMARY KEY (id)
);

CREATE TABLE student_groups
(
    id           UUID    NOT NULL,
    project_id   UUID    NOT NULL,
    group_number INTEGER NOT NULL,
    CONSTRAINT pk_student_groups PRIMARY KEY (id)
);

CREATE TABLE students
(
    user_id UUID NOT NULL,
    CONSTRAINT pk_students PRIMARY KEY (user_id)
);

CREATE TABLE subject
(
    id          UUID NOT NULL,
    name        VARCHAR(100),
    description VARCHAR(255),
    CONSTRAINT pk_subject PRIMARY KEY (id)
);

CREATE TABLE teacher
(
    user_id UUID NOT NULL,
    CONSTRAINT pk_teacher PRIMARY KEY (user_id)
);

CREATE TABLE user_info
(
    user_id       UUID NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    date_of_birth date,
    email         VARCHAR(255),
    CONSTRAINT pk_userinfo PRIMARY KEY (user_id)
);

CREATE TABLE users
(
    id            UUID                        NOT NULL,
    username      VARCHAR(100)                NOT NULL,
    password_hash VARCHAR(255)                NOT NULL,
    enabled       BOOLEAN                     NOT NULL,
    disabled_at   TIMESTAMP WITHOUT TIME ZONE,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by    UUID                        NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_by    UUID,
    version       BIGINT                      NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE project_subjects
    ADD CONSTRAINT uc_2c42a85e3f806893ddf8530c2 UNIQUE (project_id, subject_id);

ALTER TABLE user_info
    ADD CONSTRAINT uc_userinfo_email UNIQUE (email);

ALTER TABLE class_projects
    ADD CONSTRAINT ux_class_projects_class_project UNIQUE (class_id, project_id);

ALTER TABLE grades
    ADD CONSTRAINT ux_grade_once_per_student_in_project_subject UNIQUE (project_subject_id, student_id);

ALTER TABLE group_members
    ADD CONSTRAINT ux_group_members_group_user UNIQUE (group_id, student_id);

ALTER TABLE student_groups
    ADD CONSTRAINT ux_student_groups_project_groupno UNIQUE (project_id, group_number);

ALTER TABLE users
    ADD CONSTRAINT ux_users_username UNIQUE (username);

ALTER TABLE admin
    ADD CONSTRAINT FK_ADMIN_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE class_projects
    ADD CONSTRAINT FK_CLASS_PROJECTS_ON_CLASS FOREIGN KEY (class_id) REFERENCES classes (id);

CREATE INDEX ix_class_projects_class ON class_projects (class_id);

ALTER TABLE class_projects
    ADD CONSTRAINT FK_CLASS_PROJECTS_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);

CREATE INDEX ix_class_projects_project ON class_projects (project_id);

ALTER TABLE grades
    ADD CONSTRAINT FK_GRADES_ON_PROJECT_SUBJECT FOREIGN KEY (project_subject_id) REFERENCES project_subjects (id);

CREATE INDEX ix_grades_project_subject ON grades (project_subject_id);

ALTER TABLE grades
    ADD CONSTRAINT FK_GRADES_ON_STUDENT FOREIGN KEY (student_id) REFERENCES students (user_id);

CREATE INDEX ix_grades_student ON grades (student_id);

ALTER TABLE grades
    ADD CONSTRAINT FK_GRADES_ON_TEACHER FOREIGN KEY (teacher_id) REFERENCES teacher (user_id);

ALTER TABLE group_members
    ADD CONSTRAINT FK_GROUP_MEMBERS_ON_GROUP FOREIGN KEY (group_id) REFERENCES student_groups (id);

CREATE INDEX ix_group_members_group ON group_members (group_id);

ALTER TABLE group_members
    ADD CONSTRAINT FK_GROUP_MEMBERS_ON_STUDENT FOREIGN KEY (student_id) REFERENCES students (user_id);

CREATE INDEX ix_group_members_user ON group_members (student_id);

ALTER TABLE projects
    ADD CONSTRAINT FK_PROJECTS_ON_CLASS FOREIGN KEY (class_id) REFERENCES classes (id);

ALTER TABLE project_subjects
    ADD CONSTRAINT FK_PROJECT_SUBJECTS_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);

ALTER TABLE project_subjects
    ADD CONSTRAINT FK_PROJECT_SUBJECTS_ON_SUBJECT FOREIGN KEY (subject_id) REFERENCES subject (id);

ALTER TABLE student_groups
    ADD CONSTRAINT FK_STUDENT_GROUPS_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);

ALTER TABLE students
    ADD CONSTRAINT FK_STUDENT_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE teacher
    ADD CONSTRAINT FK_TEACHER_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_info
    ADD CONSTRAINT FK_USERINFO_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);