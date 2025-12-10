package de.dicecup.classlink.features.assessments.dto;

import java.util.List;
import java.util.UUID;

public record ProjectAssessmentOverviewDTO(
        UUID projectId,
        List<StudentAssessmentDetailDTO> students
) {
}
