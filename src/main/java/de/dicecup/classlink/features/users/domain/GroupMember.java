package de.dicecup.classlink.features.users.domain;

import de.dicecup.classlink.features.projects.StudentGroup;
import de.dicecup.classlink.features.users.domain.roles.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "group_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_group_members_group_user", columnNames = {"group_id", "student_id"})
        },
        indexes = {
                @Index(name = "ix_group_members_user", columnList = "student_id"),
                @Index(name = "ix_group_members_group", columnList = "group_id")
        })
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Wer ist das Mitglied (User = Schüler in diesem Kontext)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // In welcher Gruppe ist der User Mitglied?
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup studentGroup;
}
