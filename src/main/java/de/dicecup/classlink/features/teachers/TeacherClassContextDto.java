package de.dicecup.classlink.features.teachers;

import java.util.List;
import java.util.UUID;

public record TeacherClassContextDto(
        UUID classId,
        String className,
        List<TeacherTermContextDto> terms
) {
}
