ALTER TABLE students
    ADD COLUMN IF NOT EXISTS class_id uuid;
ALTER TABLE students
    ADD CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES class (id);

CREATE INDEX IF NOT EXISTS ix_students_class ON students (class_id);
