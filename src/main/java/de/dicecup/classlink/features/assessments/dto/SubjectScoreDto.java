package de.dicecup.classlink.features.assessments.dto;

import java.util.UUID;

public record SubjectScoreDto(
        UUID questionId,
        int position,
        String key,
        String label,
        Double teacherScore,
        Double selfScore,
        Double peerScoreAverage,
        int peerScoreCount,
        Tendency tendency
) {
    public enum Tendency {
        ALIGNED,
        SELF_HIGHER_THAN_PEER,
        SELF_LOWER_THAN_PEER,
        PEER_HIGHER_THAN_TEACHER,
        PEER_LOWER_THAN_TEACHER
    }
}
