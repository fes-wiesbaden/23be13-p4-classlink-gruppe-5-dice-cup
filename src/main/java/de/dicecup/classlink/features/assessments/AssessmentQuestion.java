package de.dicecup.classlink.features.assessments;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class AssessmentQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
