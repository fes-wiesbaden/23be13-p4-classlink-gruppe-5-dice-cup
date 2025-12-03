package de.dicecup.classlink.features.teachers;

import de.dicecup.classlink.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Entity
@Table(name = "teacher",
        uniqueConstraints = @UniqueConstraint(name = "teacher_id_unique", columnNames = "user_id"))
public class Teacher extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}