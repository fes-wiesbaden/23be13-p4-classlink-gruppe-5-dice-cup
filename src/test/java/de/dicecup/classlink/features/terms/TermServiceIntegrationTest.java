package de.dicecup.classlink.features.terms;

import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import de.dicecup.classlink.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TermServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TermService termService;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void onlyOneOpenTermPerSchoolYearInDatabase() {
        SchoolYear year = testDataFactory.persistActiveSchoolYear("2025/26");
        var request = new TermService.TermCreateRequest(
                "H1",
                1,
                LocalDate.now(),
                LocalDate.now().plusMonths(3)
        );

        termService.openTerm(year.getId(), request);

        assertThatThrownBy(() -> termService.openTerm(year.getId(), request))
                .isInstanceOf(IllegalStateException.class);

        List<Term> openTerms = termRepository.findBySchoolYearIdAndStatus(year.getId(), TermStatus.OPEN);
        assertThat(openTerms).hasSize(1);
    }

    @Test
    void closeTerm_persistsClosedStatus() {
        SchoolYear year = testDataFactory.persistActiveSchoolYear("2025/26");
        Term term = termService.openTerm(
                year.getId(),
                new TermService.TermCreateRequest("H1", 1, LocalDate.now(), LocalDate.now().plusMonths(3))
        );

        termService.closeTerm(term.getId());

        Term reloaded = termRepository.findById(term.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(TermStatus.CLOSED);
    }
}
