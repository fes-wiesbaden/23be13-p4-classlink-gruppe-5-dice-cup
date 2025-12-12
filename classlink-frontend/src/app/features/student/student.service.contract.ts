import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Scores } from '../teacher/models';

export interface StudentProject {
  id: string;
  name: string;
  projectGroupId?: string;
  status: 'open' | 'done';
  grade?: number;
  scores?: Scores | null;
}

export interface CurrentStudent {
  id: string;
  name: string;
  email: string;
  className?: string;
}

export interface StudentService {
  loadCurrentStudent(): Observable<CurrentStudent | null>;
  loadProjects(studentId: string): Observable<StudentProject[]>;
  loadScores(studentId: string, projectGroupId: string): Observable<Scores | null>;
}

export const STUDENT_SERVICE = new InjectionToken<StudentService>('STUDENT_SERVICE');
