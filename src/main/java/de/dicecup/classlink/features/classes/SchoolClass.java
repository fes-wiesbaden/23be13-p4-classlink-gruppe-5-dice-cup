package de.dicecup.classlink.features.classes;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 100) @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "schoolClass", fetch = FetchType.LAZY)
    private List<ClassTerm> terms;
}
