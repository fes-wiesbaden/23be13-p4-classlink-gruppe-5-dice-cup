package de.dicecup.classlink.features.teachers;

import java.util.List;
import java.util.UUID;

public record TeacherTermContextDto(
        UUID termId,
        String termName,
        boolean isCurrent,
        List<TeacherProjectGroupContextDto> projectGroups
) {
}
