package de.dicecup.classlink.features.terms;

import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;
    private final SchoolYearRepository schoolYearRepository;

    @Transactional
    public Term openTerm(UUID schoolYearId, TermCreateRequest request) {
        SchoolYear schoolYear = schoolYearRepository.findById(schoolYearId)
                .orElseThrow(() -> new EntityNotFoundException("School year not found"));
        List<Term> openTerms = termRepository.findBySchoolYearIdAndStatus(schoolYearId, TermStatus.OPEN);
        if (!openTerms.isEmpty()) {
            throw new IllegalStateException("There is already an open term for this school year");
        }
        Term term = new Term();
        term.setName(request.name());
        term.setSequenceNumber(request.sequenceNumber());
        term.setStatus(TermStatus.OPEN);
        term.setStartDate(request.startDate());
        term.setEndDate(request.endDate());
        term.setSchoolYear(schoolYear);
        return termRepository.save(term);
    }

    @Transactional
    public void closeTerm(UUID termId) {
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        term.setStatus(TermStatus.CLOSED);
    }

    @Transactional(readOnly = true)
    public List<Term> listTerms(UUID schoolYearId, TermStatus status) {
        if (schoolYearId == null) {
            if (status == null) {
                return termRepository.findAll();
            }
            return termRepository.findByStatus(status);
        }
        if (status == null) {
            return termRepository.findBySchoolYearId(schoolYearId);
        }
        return termRepository.findBySchoolYearIdAndStatus(schoolYearId, status);
    }

    public record TermCreateRequest(String name, Integer sequenceNumber, LocalDate startDate, LocalDate endDate) {
    }
}
