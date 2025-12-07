package de.dicecup.classlink.features.terms.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TermCreateWebRequest(
        @NotBlank String name,
        @NotNull Integer sequenceNumber,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
