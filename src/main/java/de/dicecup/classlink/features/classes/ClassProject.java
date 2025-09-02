package de.dicecup.classlink.features.classes;

import de.dicecup.classlink.features.projects.domain.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(
        name = "class_projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_class_projects_class_project", columnNames = {"class_id", "project_id"})
        },
        indexes = {
                @Index(name = "ix_class_projects_class", columnList = "class_id"),
                @Index(name = "ix_class_projects_project", columnList = "project_id")
        }
)
public class ClassProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class clazz;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private boolean active;

    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassProject)) return false;
        return id != null && id.equals(((ClassProject) o).id);
    }
    @Override public int hashCode() { return 31;}
}
