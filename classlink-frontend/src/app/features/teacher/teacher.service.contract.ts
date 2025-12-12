import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Assignment, ClassOption, Project, Scores, Student } from './models';

export interface TeacherService {
  loadContext(): Observable<{ classes: ClassOption[]; currentClassId: string | null; currentTermId: string | null }>;
  loadStudents(classId: string | null): Observable<Student[]>;
  loadProjects(classId: string | null, termId: string | null): Observable<Project[]>;
  loadAssignments(projectGroupId: string): Observable<Assignment[]>;
  assignStudentToProject(projectGroupId: string, studentId: string): Observable<void>;
  removeStudentFromProject(projectGroupId: string, studentId: string): Observable<void>;
  loadScores(studentId: string, projectGroupId: string): Observable<Scores | null>;
}

export const TEACHER_SERVICE = new InjectionToken<TeacherService>('TEACHER_SERVICE');
