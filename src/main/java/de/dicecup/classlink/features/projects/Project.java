package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.common.audit.Auditable;
import de.dicecup.classlink.features.classes.Class;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Project extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private Class clazz;

    @Size(max = 200)
    private String name;
    private String description;

    @Column(nullable = false)
    private boolean active = false;

    @OneToMany(mappedBy = "studentGroups", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<StudentGroup> studentGroups = new ArrayList<>();
}
