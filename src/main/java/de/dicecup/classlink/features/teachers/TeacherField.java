package de.dicecup.classlink.features.teachers;

import de.dicecup.classlink.common.audit.Auditable;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.subjects.ProjectSubject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(
        name = "teacher_field",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "subject_id"})
        }
)
public class TeacherField extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectSubject")
    @JoinColumn(name = "project_subject_id")
    private ProjectSubject projectSubject;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(precision = 10, scale = 8)
    private BigDecimal weight;
}
