CREATE TABLE questionnaires
(
    id                    UUID                        NOT NULL,
    project_id            UUID                        NOT NULL,
    status                VARCHAR(10)                 NOT NULL,
    created_by_teacher_id UUID                        NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_questionnaires PRIMARY KEY (id),
    CONSTRAINT uk_questionnaire_project UNIQUE (project_id)
);

CREATE TABLE questions
(
    id               UUID         NOT NULL,
    questionnaire_id UUID         NOT NULL,
    position         INTEGER      NOT NULL,
    text             VARCHAR(500) NOT NULL,
    active           BOOLEAN      NOT NULL,
    CONSTRAINT pk_questions PRIMARY KEY (id),
    CONSTRAINT fk_questions_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaires (id)
);

CREATE TABLE assessments
(
    id                  UUID                        NOT NULL,
    questionnaire_id    UUID                        NOT NULL,
    project_id          UUID                        NOT NULL,
    type                VARCHAR(10)                 NOT NULL,
    assessor_student_id UUID                        NOT NULL,
    assessee_student_id UUID                        NOT NULL,
    submitted_at        TIMESTAMP WITHOUT TIME ZONE,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_assessments PRIMARY KEY (id),
    CONSTRAINT fk_assessments_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaires (id)
);

CREATE TABLE assessment_answers
(
    id            UUID    NOT NULL,
    assessment_id UUID    NOT NULL,
    question_id   UUID    NOT NULL,
    score         INTEGER NOT NULL,
    CONSTRAINT pk_assessment_answers PRIMARY KEY (id),
    CONSTRAINT fk_assessment_answers_assessment FOREIGN KEY (assessment_id) REFERENCES assessments (id),
    CONSTRAINT fk_assessment_answers_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT chk_assessment_score CHECK (score >= 1 AND score <= 6)
);

CREATE UNIQUE INDEX uk_assessment_self ON assessments (questionnaire_id, type, assessor_student_id) WHERE type = 'SELF';
CREATE UNIQUE INDEX uk_assessment_peer ON assessments (questionnaire_id, type, assessor_student_id, assessee_student_id) WHERE type = 'PEER';
