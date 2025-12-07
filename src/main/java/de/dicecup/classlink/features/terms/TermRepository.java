package de.dicecup.classlink.features.terms;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TermRepository extends JpaRepository<Term, UUID> {
    List<Term> findBySchoolYearId(UUID schoolYearId);

    List<Term> findBySchoolYearIdAndStatus(UUID schoolYearId, TermStatus status);

    List<Term> findByStatus(TermStatus status);
}
