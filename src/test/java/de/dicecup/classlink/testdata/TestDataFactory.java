package de.dicecup.classlink.testdata;

import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.ClassRepository;
import de.dicecup.classlink.features.classes.ClassTerm;
import de.dicecup.classlink.features.classes.ClassTermRepository;
import de.dicecup.classlink.features.classes.ClassTermStatus;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.projects.ProjectRepository;
import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.terms.TermStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    private final ClassRepository classRepository;
    private final ClassTermRepository classTermRepository;
    private final ProjectRepository projectRepository;
    private final TermRepository termRepository;
    private final SchoolYearRepository schoolYearRepository;

    public SchoolYear persistActiveSchoolYear(String name) {
        SchoolYear schoolYear = TestFixtures.activeSchoolYear(name);
        return schoolYearRepository.save(schoolYear);
    }

    public SchoolClass persistClass(String name) {
        return persistClass(name, new ArrayList<>());
    }

    public SchoolClass persistClass(String name, List<ClassTerm> classTerms) {
        SchoolClass clazz = TestFixtures.schoolClass(name, classTerms);
        return classRepository.save(clazz);
    }

    public Term persistOpenTerm(String name, SchoolYear year) {
        Term term = TestFixtures.openTerm(name, year);
        return termRepository.save(term);
    }

    public ClassTerm assign(SchoolClass schoolClass, Term term, ClassTermStatus status) {
        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(schoolClass);
        classTerm.setTerm(term);
        classTerm.setStatus(status);
        return classTermRepository.save(classTerm);
    }

    public Term persistTerm(String name, List<ClassTerm> classTerms, List<Project> projects, SchoolYear year, TermStatus status, LocalDate startDate, LocalDate endDate) {
        Term term = TestFixtures.term(name, classTerms, projects, year, status, startDate, endDate);
        return termRepository.save(term);
    }

    public Project persistProject(String name, SchoolClass schoolClass, Term term) {
        Project project = TestFixtures.project(name, schoolClass, term);
        return projectRepository.save(project);
    }

    public Project persistProject(String name, boolean active, String description, List<ProjectGroup> projectGroups, SchoolClass clazz, Term term) {
        Project p = TestFixtures.project(name, active, description, projectGroups, clazz, term);
        return projectRepository.save(p);
    }

}
