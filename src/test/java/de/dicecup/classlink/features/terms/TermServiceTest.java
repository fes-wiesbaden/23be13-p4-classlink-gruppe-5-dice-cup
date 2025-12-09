package de.dicecup.classlink.features.terms;

import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.terms.TermService;
import de.dicecup.classlink.features.terms.TermStatus;
import de.dicecup.classlink.testdata.TestFixtures;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TermServiceTest {

    @Mock
    TermRepository termRepository;
    @Mock
    SchoolYearRepository schoolYearRepository;

    @InjectMocks
    TermService termService;

    @Test
    void openTerm_allowsWhenOnlyClosedTermsExists() {
        UUID schoolYearId = UUID.randomUUID();
        SchoolYear year = TestFixtures.activeSchoolYear("2025/26");
        year.setId(schoolYearId);

        when(schoolYearRepository.findById(schoolYearId))
                .thenReturn(Optional.of(year));

        when(termRepository.findBySchoolYearIdAndStatus(schoolYearId, TermStatus.OPEN))
                .thenReturn(List.of());

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);
        var request = new TermService.TermCreateRequest("H2", 1, start, end);

        Term persisted = TestFixtures.openTerm("H3", year);
        persisted.setId(UUID.randomUUID());
        when(termRepository.save(any(Term.class))).thenReturn(persisted);

        // Act
        Term result = termService.openTerm(schoolYearId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TermStatus.OPEN);
        assertThat(result.getSchoolYear().getId()).isEqualTo(schoolYearId);

        verify(termRepository).findBySchoolYearIdAndStatus(schoolYearId, TermStatus.OPEN);
        verify(termRepository).save(any(Term.class));
    }

    @Test
    void openTerm_throwsWhenSchoolYearNotFound() {
        UUID schoolYearId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);
        var request = new TermService.TermCreateRequest("H1", 1, start, end);

        when(schoolYearRepository.findById(schoolYearId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> termService.openTerm(schoolYearId, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(termRepository, never()).save(any());
    }

    @Test
    void openTerm_throwsWhenOpenTermAlreadyExistsForSchoolYear() {
        UUID schoolYearId = UUID.randomUUID();
        SchoolYear year = TestFixtures.activeSchoolYear("2025/26");
        year.setId(schoolYearId);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);
        var request = new TermService.TermCreateRequest("H1", 1, start, end);

        when(schoolYearRepository.findById(schoolYearId)).thenReturn(Optional.of(year));
        when(termRepository.findBySchoolYearIdAndStatus(schoolYearId, TermStatus.OPEN))
                .thenReturn(List.of(TestFixtures.openTerm("Existing", year)));

        assertThatThrownBy(() -> termService.openTerm(schoolYearId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already an open term");

        verify(termRepository, never()).save(any());
    }

    @Test
    void closeTerm_setsStatusClosed() {
        UUID termId = UUID.randomUUID();
        Term term = new Term();
        term.setId(termId);
        term.setStatus(TermStatus.OPEN);

        when(termRepository.findById(termId)).thenReturn(Optional.of(term));

        termService.closeTerm(termId);

        assertThat(term.getStatus()).isEqualTo(TermStatus.CLOSED);
        verify(termRepository).findById(termId);
        verify(termRepository, never()).save(any());
    }

    @Test
    void closeTerm_keepsStatusWhenAlreadyClosed() {
        UUID termId = UUID.randomUUID();
        Term term = new Term();
        term.setId(termId);
        term.setStatus(TermStatus.CLOSED);

        when(termRepository.findById(termId)).thenReturn(Optional.of(term));

        termService.closeTerm(termId);

        assertThat(term.getStatus()).isEqualTo(TermStatus.CLOSED);
        verify(termRepository).findById(termId);
        verify(termRepository, never()).save(any());
    }


}
