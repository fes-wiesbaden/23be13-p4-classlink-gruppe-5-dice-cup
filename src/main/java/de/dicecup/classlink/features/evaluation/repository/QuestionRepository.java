package de.dicecup.classlink.features.evaluation.repository;

import de.dicecup.classlink.features.evaluation.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}
