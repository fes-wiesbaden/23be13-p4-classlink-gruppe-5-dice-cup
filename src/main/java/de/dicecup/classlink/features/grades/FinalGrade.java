package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.common.audit.Auditable;
import de.dicecup.classlink.features.schoolyear.Schoolyear;
import de.dicecup.classlink.features.subjects.Subject;
import de.dicecup.classlink.features.users.domain.roles.Student;
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
@Table(name = "final_grades",
        uniqueConstraints = {
                @UniqueConstraint(name = "assignment_once_per_student",
                        columnNames = {"subject_id", "student_id"})
        })
public class FinalGrade extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schoolyear", nullable = false)
    private Schoolyear year;

    @Column(name = "grade_value", precision = 2, scale = 1, nullable = false)
    private BigDecimal gradeValue;
}
