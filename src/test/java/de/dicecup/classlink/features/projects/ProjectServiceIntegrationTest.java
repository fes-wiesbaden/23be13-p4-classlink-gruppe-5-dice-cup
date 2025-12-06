package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.classes.ClassTermStatus;
import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import de.dicecup.classlink.testdata.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void createProject_persistsWithClassAndTerm() {
        SchoolYear year = testDataFactory.persistActiveSchoolYear("2025/26");
        Class schoolClass = testDataFactory.persistClass("FI AE 23");
        Term term = testDataFactory.persistOpenTerm("H1", year);
        testDataFactory.assign(schoolClass, term, ClassTermStatus.ACTIVE);

        Project project = projectService.createProject(
                schoolClass.getId(),
                term.getId(),
                new ProjectService.ProjectRequest("Projekt 1", "Desc", null)
        );

        Project reloaded = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(reloaded.getSchoolClass().getId()).isEqualTo(schoolClass.getId());
        assertThat(reloaded.getTerm().getId()).isEqualTo(term.getId());
        assertThat(reloaded.isActive()).isTrue();
        assertThat(reloaded.getName()).isEqualTo("Projekt 1");
    }

    @Test
    void createProject_failsWhenClassNotAssignedToTerm() {
        SchoolYear year = testDataFactory.persistActiveSchoolYear("2025/26");
        Class schoolClass = testDataFactory.persistClass("FI AE 23");
        Term term = testDataFactory.persistOpenTerm("H1", year);

        assertThatThrownBy(() -> projectService.createProject(
                schoolClass.getId(),
                term.getId(),
                new ProjectService.ProjectRequest("Projekt 1", "Desc", null)
        )).isInstanceOf(EntityNotFoundException.class);

        assertThat(projectRepository.count()).isZero();
    }
}
