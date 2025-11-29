package de.dicecup.classlink.features.projects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "student_groups",
uniqueConstraints = {
        @UniqueConstraint(name = "ux_student_groups_project_groupno",
                columnNames = {"project_id", "group_number"})
})
public class StudentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "group_number", nullable = false)
    private Integer groupNumber;
}
