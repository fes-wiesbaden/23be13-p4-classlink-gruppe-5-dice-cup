package de.dicecup.classlink.features.teachers;

import java.util.List;
import java.util.UUID;

public record TeacherContextDto(
        UUID teacherId,
        List<TeacherClassContextDto> classes
) {
}
