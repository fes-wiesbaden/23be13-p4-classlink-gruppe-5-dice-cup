package de.dicecup.classlink.features.subjects;

import de.dicecup.classlink.common.audit.Auditable;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.teachers.TeacherField;
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
@Table(
        name = "project_subject",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"teacher_field_id", "subject_id"})
        }
)
public class ProjectSubject extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_field_id", nullable = false)
    private TeacherField teacherField;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(precision = 10, scale = 8, nullable = false)
    private BigDecimal weight;
}
