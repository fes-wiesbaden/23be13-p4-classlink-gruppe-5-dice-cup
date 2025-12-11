package de.dicecup.classlink.features.subjects.web;

import de.dicecup.classlink.features.subjects.Subject;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SubjectDto(
        UUID id,
        String name,
        String description
) {
    public static SubjectDto from(Subject subject) {
        return new SubjectDto(
                subject.getId(),
                subject.getName(),
                subject.getDescription()
        );
    }
}
