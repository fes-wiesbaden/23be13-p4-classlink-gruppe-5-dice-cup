package de.dicecup.classlink.features.assessments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    Optional<Assessment> findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(UUID questionnaireId,
                                                                                              AssessmentType type,
                                                                                              UUID assessorId,
                                                                                              UUID assesseeId);
}
