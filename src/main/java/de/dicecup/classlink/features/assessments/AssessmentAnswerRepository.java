package de.dicecup.classlink.features.assessments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, UUID> {

    @Query("""
            select aa from AssessmentAnswer aa
            join fetch aa.assessment a
            where a.questionnaire.id = :questionnaireId
            and a.submittedAt is not null
            """)
    List<AssessmentAnswer> findSubmittedByQuestionnaire(@Param("questionnaireId") UUID questionnaireId);

    List<AssessmentAnswer> findByAssessmentId(UUID assessmentId);
}
