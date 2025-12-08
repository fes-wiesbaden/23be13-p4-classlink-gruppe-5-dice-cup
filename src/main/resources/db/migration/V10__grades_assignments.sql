ALTER TABLE assessment_answers
    DROP CONSTRAINT fk_assessment_answers_assessment;

ALTER TABLE assessment_answers
    DROP CONSTRAINT fk_assessment_answers_question;

ALTER TABLE assessments
    DROP CONSTRAINT fk_assessments_questionnaire;

ALTER TABLE grades
    DROP CONSTRAINT fk_grades_on_project_subject;

ALTER TABLE project
    DROP CONSTRAINT fk_project_responsible_teacher;

ALTER TABLE questions
    DROP CONSTRAINT fk_questions_questionnaire;

CREATE TABLE assessment_question
(
    id UUID NOT NULL,
    CONSTRAINT pk_assessmentquestion PRIMARY KEY (id)
);

CREATE TABLE final_grade_assignments
(
    id              UUID                        NOT NULL,
    assignment_name VARCHAR(255)                NOT NULL,
    class_id        UUID                        NOT NULL,
    term_id         UUID                        NOT NULL,
    subject_id      UUID                        NOT NULL,
    teacher_id      UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_final_grade_assignments PRIMARY KEY (id)
);

ALTER TABLE class_subject_assignments
    ADD assignment_name VARCHAR(255);

ALTER TABLE class_subject_assignments
    ALTER COLUMN assignment_name SET NOT NULL;

ALTER TABLE grades
    ADD class_subject_assignment_id UUID;

ALTER TABLE grades
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE grades
    ALTER COLUMN class_subject_assignment_id SET NOT NULL;

ALTER TABLE grades
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE grades
    ADD CONSTRAINT grade_once_per_assignment_and_student UNIQUE (class_subject_assignment_id, student_id);

ALTER TABLE final_grade_assignments
    ADD CONSTRAINT uk_class_subject_assignment UNIQUE (class_id, term_id, subject_id, teacher_id);

ALTER TABLE final_grade_assignments
    ADD CONSTRAINT FK_FINAL_GRADE_ASSIGNMENTS_ON_CLASS FOREIGN KEY (class_id) REFERENCES class (id);

ALTER TABLE final_grade_assignments
    ADD CONSTRAINT FK_FINAL_GRADE_ASSIGNMENTS_ON_SUBJECT FOREIGN KEY (subject_id) REFERENCES subject (id);

ALTER TABLE final_grade_assignments
    ADD CONSTRAINT FK_FINAL_GRADE_ASSIGNMENTS_ON_TEACHER FOREIGN KEY (teacher_id) REFERENCES teacher (user_id);

ALTER TABLE final_grade_assignments
    ADD CONSTRAINT FK_FINAL_GRADE_ASSIGNMENTS_ON_TERM FOREIGN KEY (term_id) REFERENCES terms (id);

ALTER TABLE grades
    ADD CONSTRAINT FK_GRADES_ON_CLASS_SUBJECT_ASSIGNMENT FOREIGN KEY (class_subject_assignment_id) REFERENCES class_subject_assignments (id);

DROP TABLE assessment_answers CASCADE;

DROP TABLE assessments CASCADE;

DROP TABLE questionnaires CASCADE;

DROP TABLE questions CASCADE;

ALTER TABLE project
    DROP COLUMN created_by;

ALTER TABLE project
    DROP COLUMN responsible_teacher_id;

ALTER TABLE grades
    DROP COLUMN project_subject_id;

ALTER TABLE grades
    DROP COLUMN grade_value;

ALTER TABLE project
    ALTER COLUMN class_id SET NOT NULL;

ALTER TABLE grades
    ADD grade_value DECIMAL(2, 1) NOT NULL;

ALTER TABLE class_subject_assignments
    ALTER COLUMN weighting TYPE DECIMAL USING (weighting::DECIMAL);