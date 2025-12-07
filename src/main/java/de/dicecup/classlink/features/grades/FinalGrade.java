package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.classes.ClassFinalGradeAssignment;
import de.dicecup.classlink.features.classes.ClassSubjectAssignment;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "grades",
        uniqueConstraints= {
                @UniqueConstraint(name = "grade_once_per_assignment_and_student",
                        columnNames = {"class_subject_assignment_id", "student_id"})
        })
public class FinalGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_final_grade_assignment_id", nullable = false)
    private ClassFinalGradeAssignment classFinalGradeAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "grade_value", nullable = false)
    private BigDecimal gradeValue;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Handling für vom System erstellten Noten nötig
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher changedBy;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {

        this.updatedAt = Instant.now();
    }
}
