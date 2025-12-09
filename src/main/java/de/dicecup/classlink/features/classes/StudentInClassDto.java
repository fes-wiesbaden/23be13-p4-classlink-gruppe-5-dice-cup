package de.dicecup.classlink.features.classes;

import java.util.UUID;

public record StudentInClassDto(
        UUID studentId,
        String firstName,
        String lastName,
        UUID classId,
        String className
) {
}
