package de.dicecup.classlink.features.assessments.dto;

import java.util.UUID;

public record ProjectGroupStudentScoreOverviewDto(
        UUID studentId,
        String studentName,
        UUID classId,
        String className,
        UUID projectGroupId,
        String projectName,
        Double teacherScore,
        Double selfScore,
        Double peerScore,
        Tendency tendency
) {
    public enum Tendency {
        ALIGNED,
        SELF_HIGHER_THAN_TEACHER,
        SELF_LOWER_THAN_TEACHER,
        PEER_HIGHER_THAN_TEACHER,
        PEER_LOWER_THAN_TEACHER
    }
}
