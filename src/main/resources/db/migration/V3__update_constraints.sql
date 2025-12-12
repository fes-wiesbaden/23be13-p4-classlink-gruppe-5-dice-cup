ALTER TABLE class_subject_assignments
ALTER
COLUMN weighting TYPE DECIMAL(3, 1) USING (weighting::DECIMAL(3, 1));

ALTER TABLE class_subject_assignments
    ALTER COLUMN weighting SET NOT NULL;

ALTER TABLE class_subject_assignments
DROP
CONSTRAINT uk_class_subject_assignment;

ALTER TABLE class_subject_assignments
    ADD CONSTRAINT uk_class_subject_assignment UNIQUE (assignment_name, school_class_id, term_id, subject_id, teacher_id);

ALTER TABLE final_grade_assignments
DROP
CONSTRAINT uk_final_grade_assignment;

ALTER TABLE final_grade_assignments
    ADD CONSTRAINT uk_final_grade_assignment UNIQUE (assignment_name, school_class_id, term_id, subject_id, teacher_id);