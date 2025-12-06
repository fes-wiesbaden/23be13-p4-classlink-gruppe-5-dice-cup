package de.dicecup.classlink.features.schoolyear;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "schoolyear",
        uniqueConstraints = {
                @UniqueConstraint(name = "schoolyear_is_unique",
                        columnNames = {"year", "term"})
        })
public class Schoolyear {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Wir gehen hier vom Jahr aus in dem das Schuljahr startet, 24/25 entspricht 2024
    @Column(name = "year", nullable = false)
    private int year;

    // Kann 1 oder 2 sein
    @Column(name = "term", nullable = false)
    private int term;
}
