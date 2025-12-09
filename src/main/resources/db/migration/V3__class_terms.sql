CREATE TABLE class_terms
(
    id         UUID                        NOT NULL,
    school_class_id   UUID                        NOT NULL,
    term_id    UUID                        NOT NULL,
    status     VARCHAR(10)                 NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_class_terms PRIMARY KEY (id)
);

CREATE TABLE terms
(
    id         UUID                        NOT NULL,
    name       VARCHAR(25)                 NOT NULL,
    status     SMALLINT                    NOT NULL,
    start_date date                        NOT NULL,
    end_date   date                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_terms PRIMARY KEY (id)
);

ALTER TABLE class_terms
    ADD CONSTRAINT uk_class_term_class_term UNIQUE (school_class_id, term_id);

ALTER TABLE class_terms
    ADD CONSTRAINT FK_CLASS_TERMS_ON_CLASS FOREIGN KEY (school_class_id) REFERENCES school_class (id);

ALTER TABLE class_terms
    ADD CONSTRAINT FK_CLASS_TERMS_ON_TERM FOREIGN KEY (term_id) REFERENCES terms (id);
