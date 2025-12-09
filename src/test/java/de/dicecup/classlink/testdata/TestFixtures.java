package de.dicecup.classlink.testdata;

import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.classes.ClassTerm;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearStatus;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestFixtures {

    private TestFixtures() {
    }

    public static SchoolYear activeSchoolYear(String name) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(10);
        return schoolYear(name, SchoolYearStatus.ACTIVE, new ArrayList<>(), startDate, endDate);
    }

    public static Term openTerm(String name, SchoolYear year) {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = startDate.plusMonths(3);
        return term(name, new ArrayList<>(), new ArrayList<>(), year, TermStatus.OPEN, startDate, endDate);
    }

    public static Term closeTerm(String name, SchoolYear year) {
        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusMonths(3);
        return term(name, new ArrayList<>(), new ArrayList<>(), year, TermStatus.CLOSED, startDate, endDate);
    }

    public static Class schoolClass(String name) {
        return schoolClass(name, new ArrayList<>());
    }

    public static Project project(String name, Class clazz, Term term) {
        return project(name, true, "Test project", new ArrayList<>(), clazz, term);
    }

    public static Class schoolClass(String name, List<ClassTerm> classTerms) {
        Class clazz = new Class();
        clazz.setName(name);
        clazz.setTerms(classTerms);
        return clazz;
    }

    public static SchoolYear schoolYear(String name, SchoolYearStatus status, List<Term> terms, LocalDate startDate, LocalDate endDate) {
        SchoolYear year = new SchoolYear();
        year.setName(name);
        year.setStatus(status);
        year.setTerms(terms);
        year.setStartDate(startDate);
        year.setEndDate(endDate);
        return year;
    }

    public static Term term(String name, List<ClassTerm> classTerms, List<Project> projects, SchoolYear year,
                            TermStatus status, LocalDate startDate, LocalDate endDate) {
        Term t = new Term();
        t.setName(name);
        t.setClassTerms(classTerms);
        t.setProjects(projects);
        t.setSchoolYear(year);
        t.setStatus(status);
        t.setStartDate(startDate);
        t.setEndDate(endDate);
        return t;
    }

    public static Project project(String name, boolean active, String description, List<ProjectGroup> projectGroups, Class clazz, Term term) {
        Project p = new Project();
        p.setName(name);
        p.setActive(active);
        p.setDescription(description);
        p.setProjectGroups(projectGroups);
        p.setSchoolClass(clazz);
        p.setTerm(term);
        return p;
    }

}
