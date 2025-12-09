package de.dicecup.classlink.features.schoolyear;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolYearServiceTest {

    @Mock
    private SchoolYearRepository schoolYearRepository;

    @InjectMocks
    private SchoolYearService schoolYearService;

    @Test
    void create_setsStatusActiveAndPersists() {
        LocalDate start = LocalDate.of(2024, 9, 1);
        LocalDate end = LocalDate.of(2025, 7, 31);
        var request = new SchoolYearService.SchoolYearRequest("2024/25", start, end);

        when(schoolYearRepository.save(any(SchoolYear.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SchoolYear result = schoolYearService.create(request);

        assertThat(result.getName()).isEqualTo("2024/25");
        assertThat(result.getStartDate()).isEqualTo(start);
        assertThat(result.getEndDate()).isEqualTo(end);
        assertThat(result.getStatus()).isEqualTo(SchoolYearStatus.ACTIVE);
    }

    @Test
    void close_throwsWhenSchoolYearNotFound() {
        UUID id = UUID.randomUUID();
        when(schoolYearRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolYearService.close(id))
                .isInstanceOf(EntityNotFoundException.class);

        verify(schoolYearRepository, never()).save(any());
    }

    @Test
    void close_setsStatusClosed() {
        UUID id = UUID.randomUUID();
        SchoolYear schoolYear = new SchoolYear();
        schoolYear.setId(id);
        schoolYear.setStatus(SchoolYearStatus.ACTIVE);

        when(schoolYearRepository.findById(id)).thenReturn(Optional.of(schoolYear));

        SchoolYear result = schoolYearService.close(id);

        assertThat(result.getStatus()).isEqualTo(SchoolYearStatus.CLOSED);
        verify(schoolYearRepository).findById(id);
    }
}
