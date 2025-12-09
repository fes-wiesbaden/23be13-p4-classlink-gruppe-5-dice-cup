package de.dicecup.classlink.features.schoolyear.web;

import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearStatus;

import java.time.LocalDate;
import java.util.UUID;

public record SchoolYearDto(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SchoolYearStatus status
) {
    public static SchoolYearDto from(SchoolYear year) {
        return new SchoolYearDto(
                year.getId(),
                year.getName(),
                year.getStartDate(),
                year.getEndDate(),
                year.getStatus()
        );
    }
}
