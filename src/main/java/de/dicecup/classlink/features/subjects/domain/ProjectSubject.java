package de.dicecup.classlink.features.subjects.domain;

import de.dicecup.classlink.features.projects.domain.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(
        name = "project_subject",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "subject_id"})
        }
)
public class ProjectSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal weight;
}
