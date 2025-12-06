package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.common.audit.Auditable;
import de.dicecup.classlink.features.schoolyear.Schoolyear;
import de.dicecup.classlink.features.subjects.Subject;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.classes.Class;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "grade_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "all_assignments_unique",
                        columnNames = {"assignment_name", "subject_id", "class_id", "teacher_id", "schoolyear_id"})
        })
public class GradeAssignment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "assignment_name", nullable = false)
    private String assignmentName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id")
    private Class clazz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schoolyear_id", nullable = false)
    private Schoolyear schoolyear;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal weight;
}
