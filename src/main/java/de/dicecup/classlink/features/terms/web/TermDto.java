package de.dicecup.classlink.features.terms.web;

import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TermDto(
        UUID id,
        String name,
        Integer sequenceNumber,
        LocalDate startDate,
        LocalDate endDate,
        TermStatus status,
        UUID schoolYearId
) {
    public static TermDto from(Term term) {
        return new TermDto(
                term.getId(),
                term.getName(),
                term.getSequenceNumber(),
                term.getStartDate(),
                term.getEndDate(),
                term.getStatus(),
                term.getSchoolYear() != null ? term.getSchoolYear().getId() : null
        );
    }
}
