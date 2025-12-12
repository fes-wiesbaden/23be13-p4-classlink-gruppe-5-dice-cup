import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { catchError, finalize, of } from 'rxjs';
import { STUDENT_SERVICE, CurrentStudent, StudentProject, StudentService } from '../../features/student/student.service.contract';
import { StudentApiService } from '../../features/student/student.api.service';

@Component({
  standalone: true,
  selector: 'app-student',
  imports: [CommonModule, Button, Toast],
  templateUrl: './student.html',
  styleUrl: './student.scss',
  providers: [
    MessageService,
    { provide: STUDENT_SERVICE, useExisting: StudentApiService },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudentComponent implements OnInit {
  private readonly studentSvc = inject<StudentService>(STUDENT_SERVICE);

  currentStudent = signal<CurrentStudent | null>(null);
  projects = signal<StudentProject[]>([]);
  selectedProjectId = signal<string | null>(null);
  apiError = signal<string | null>(null);
  busy = signal(false);

  averageGrade = computed(() => {
    const scores = this.projects()
      .map((p) => p.scores?.teacher)
      .filter((v): v is string => v !== undefined && v !== null);
    if (!scores.length) return null;
    const vals = scores.map((v) => Number(String(v)));
    const valid = vals.filter((v) => !isNaN(v));
    if (!valid.length) return null;
    return Number((valid.reduce((acc, v) => acc + v, 0) / valid.length).toFixed(1));
  });
  openProjects = computed(() => this.projects().filter((p) => p.status === 'open').length);

  ngOnInit(): void {
    this.loadCurrentStudent();
  }

  trackByProject(_: number, p: StudentProject) {
    return p.id;
  }

  private loadCurrentStudent(): void {
    this.busy.set(true);
    this.apiError.set(null);
    this.studentSvc
      .loadCurrentStudent()
      .pipe(
        finalize(() => this.busy.set(false)),
        catchError((err) => {
          console.error('Failed to load student profile', err);
          this.apiError.set('Profil konnte nicht geladen werden.');
          return of(null);
        }),
      )
      .subscribe((p) => {
        if (!p) {
          return;
        }
        this.currentStudent.set(p);
        if (p.id) {
          this.loadProjects(p.id);
        }
      });
  }

  private loadProjects(studentId: string) {
    this.studentSvc.loadProjects(studentId).subscribe((projects) => {
      this.projects.set(projects);
      if (!this.selectedProjectId() && projects.length) {
        this.selectedProjectId.set(projects[0].id);
      } else if (!projects.length) {
        this.selectedProjectId.set(null);
      }
      projects.forEach((p) => {
        if (!p.projectGroupId) return;
        this.studentSvc.loadScores(studentId, p.projectGroupId).subscribe((score) => {
          if (!score) return;
          this.projects.update((list) =>
            list.map((it) => (it.id === p.id ? { ...it, scores: score } : it)),
          );
        });
      });
    });
  }
}
