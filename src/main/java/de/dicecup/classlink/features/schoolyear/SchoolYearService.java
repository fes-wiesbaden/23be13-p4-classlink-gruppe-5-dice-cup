package de.dicecup.classlink.features.schoolyear;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolYearService {

    private final SchoolYearRepository schoolYearRepository;

    @Transactional(readOnly = true)
    public List<SchoolYear> list(SchoolYearStatus status) {
        return status == null ? schoolYearRepository.findAll() : schoolYearRepository.findByStatus(status);
    }

    @Transactional
    public SchoolYear create(SchoolYearRequest request) {
        SchoolYear schoolYear = new SchoolYear();
        schoolYear.setName(request.name());
        schoolYear.setStartDate(request.startDate());
        schoolYear.setEndDate(request.endDate());
        schoolYear.setStatus(SchoolYearStatus.ACTIVE);
        return schoolYearRepository.save(schoolYear);
    }

    @Transactional
    public SchoolYear close(UUID schoolYearId) {
        SchoolYear schoolYear = schoolYearRepository.findById(schoolYearId)
                .orElseThrow(() -> new EntityNotFoundException("School year not found"));
        schoolYear.setStatus(SchoolYearStatus.CLOSED);
        return schoolYear;
    }

    public record SchoolYearRequest(String name, LocalDate startDate, LocalDate endDate) {
    }
}
