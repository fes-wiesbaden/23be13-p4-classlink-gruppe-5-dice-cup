package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.subjects.ProjectSubject;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_grade_once_per_student_in_project_subject",
                        columnNames = {"project_subject_id", "student_id"})
        },
        indexes = {
                @Index(name = "ix_grades_student", columnList = "student_id"),
                @Index(name = "ix_grades_project_subject", columnList = "project_subject_id")
        })
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_subject_id", nullable = false)
    private ProjectSubject projectSubject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @DecimalMin("1.0") @DecimalMax("6.0")
    @Column(name = "grade_value", nullable = false)
    private Double gradeValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
