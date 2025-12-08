package de.dicecup.classlink.features.teachers;

import java.util.UUID;

public record TeacherProjectGroupContextDto(
        UUID projectGroupId,
        String projectName,
        int studentCount
) {
}
