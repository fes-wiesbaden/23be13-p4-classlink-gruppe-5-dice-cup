CREATE TABLE class_subject_assignments
(
    id         UUID                        NOT NULL,
    class_id   UUID                        NOT NULL,
    term_id    UUID                        NOT NULL,
    subject_id UUID                        NOT NULL,
    teacher_id UUID                        NOT NULL,
    weighting  NUMERIC,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_class_subject_assignments PRIMARY KEY (id),
    CONSTRAINT uk_class_subject_assignment UNIQUE (class_id, term_id, subject_id, teacher_id)
);

ALTER TABLE class_subject_assignments
    ADD CONSTRAINT fk_class_subject_assignment_class FOREIGN KEY (class_id) REFERENCES class (id);

ALTER TABLE class_subject_assignments
    ADD CONSTRAINT fk_class_subject_assignment_term FOREIGN KEY (term_id) REFERENCES terms (id);

ALTER TABLE class_subject_assignments
    ADD CONSTRAINT fk_class_subject_assignment_subject FOREIGN KEY (subject_id) REFERENCES subject (id);

ALTER TABLE class_subject_assignments
    ADD CONSTRAINT fk_class_subject_assignment_teacher FOREIGN KEY (teacher_id) REFERENCES teacher (user_id);
