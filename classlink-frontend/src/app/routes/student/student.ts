import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../services/auth.service';
import { TeacherMockService } from '../../features/teacher/mock.service';
import { Scores } from '../../features/teacher/models';
import { UserControllerService, UserDto } from '../../api';
import { finalize, take } from 'rxjs';

interface Assessment {
  label: string;
  grade: number;
  weight: number;
  type: 'Test' | 'Projekt' | 'Mitarbeit';
}

interface Lernfeld {
  id: number;
  title: string;
  focus: string;
  assessments: Assessment[];
  average: number;
  trend: string;
}

interface StudentProject {
  id: number;
  name: string;
  role: string;
  nextDue: string;
  progress: number;
  scores: Scores;
  peerDone: boolean;
  color: string;
}

@Component({
  standalone: true,
  selector: 'app-student',
  imports: [CommonModule, Button, Toast],
  templateUrl: './student.html',
  styleUrl: './student.scss',
  providers: [MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudentComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly mock = inject(TeacherMockService);
  private readonly messages = inject(MessageService);
  private readonly usersApi = inject(UserControllerService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly studentId = 1;
  studentName: string;
  studentEmail = '';
  studentClass = '10A';
  lernfelder: Lernfeld[] = [];
  projects: StudentProject[] = [];
  averageGrade = 0;
  openProjects = 0;
  selectedLernfeld: Lernfeld | null = null;
  apiUser: UserDto | null = null;
  userLoading = false;
  userLoadError = false;

  constructor() {
    const rawUser = this.auth.getUsername();
    this.studentName = this.extractDisplayName(rawUser);
    this.studentEmail = this.makeEmail(rawUser ?? this.studentName);
    const cls = this.mock.getStudents().find((s) => s.id === this.studentId)?.class;
    if (cls) {
      this.studentClass = cls;
    }
    this.lernfelder = this.buildLernfelder();
    this.averageGrade = Number(
      (this.lernfelder.reduce((acc, lf) => acc + lf.average, 0) / this.lernfelder.length).toFixed(
        1,
      ),
    );
    this.projects = this.buildProjects();
    this.openProjects = this.projects.filter((p) => !p.peerDone).length;
    this.selectedLernfeld = this.lernfelder[0] ?? null;
  }

  ngOnInit(): void {
    this.loadStudentFromApi();
  }

  trackByLernfeld(_: number, lf: Lernfeld) {
    return lf.id;
  }

  trackByProject(_: number, p: StudentProject) {
    return p.id;
  }

  selectLernfeld(lf: Lernfeld): void {
    this.selectedLernfeld = lf;
  }

  isSelected(id: number): boolean {
    return this.selectedLernfeld?.id === id;
  }

  gradeProgress(grade: number): number {
    const clamped = Math.min(6, Math.max(1, grade));
    const normalized = (6 - clamped) / 5;
    return Math.round(normalized * 100);
  }

  startPeerEvaluation(project: StudentProject): void {
    project.peerDone = true;
    this.openProjects = this.projects.filter((p) => !p.peerDone).length;
    this.messages.add({
      severity: 'success',
      summary: 'Peer-Evaluation',
      detail: `${project.name}: teile dein Feedback.`,
    });
  }

  private makeEmail(name: string): string {
    if (!name) {
      return 'student@dicecup.local';
    }
    const trimmed = name.trim().toLowerCase();
    if (trimmed.includes('@')) {
      return trimmed;
    }
    const sanitized = name
      .toLowerCase()
      .replace(/[^a-z0-9]+/gi, '.')
      .replace(/\.+/g, '.')
      .replace(/^\.+|\.+$/g, '');
    return `${sanitized || 'student'}@dicecup.local`;
  }

  private buildProjects(): StudentProject[] {
    const assignments = this.mock
      .getAssignments()
      .filter((a) => a.studentId === this.studentId && a.assigned);
    const projects = this.mock.getProjects();
    const assignedProjects = assignments.length
      ? assignments
          .map((a) => projects.find((p) => p.id === a.projectId))
          .filter((p): p is NonNullable<typeof p> => !!p)
      : projects.slice(0, 2);

    const palette = [
      'linear-gradient(120deg, #7c3aed, #38bdf8)',
      'linear-gradient(120deg, #22c55e, #4f46e5)',
      'linear-gradient(120deg, #f59e0b, #6366f1)',
    ];

    return assignedProjects.map((p, idx) => {
      const scores = this.mock.getScores(this.studentId, p.id);
      return {
        id: p.id,
        name: p.name,
        role: ['Team Delta', 'Team Nova', 'Team Aurora'][idx % 3],
        nextDue: ['12.12.', '18.12.', '08.01.'][idx % 3],
        progress: 65 + ((idx * 11) % 25),
        scores,
        peerDone: false,
        color: palette[idx % palette.length],
      };
    });
  }

  /**
   * Load student data from backend; show an error if it fails
   */
  private loadStudentFromApi(): void {
    this.userLoading = true;
    this.userLoadError = false;
    this.usersApi
      .getUsers()
      .pipe(
        take(1),
        finalize(() => {
          this.userLoading = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (users) => {
          const user = users.find((u) => u.id) ?? users[0];
          if (!user) {
            this.userLoadError = true;
            return;
          }
          this.apiUser = user;
          const info = user.userInfo;
          const fullName =
            info?.firstName && info?.lastName
              ? `${info.firstName} ${info.lastName}`
              : this.extractDisplayName(user.username);
          this.studentName = fullName || this.studentName;
          this.studentEmail = info?.email
            ? info.email
            : this.makeEmail(user.username ?? this.studentName);
        },
        error: (error) => {
          console.error('Failed to load student from API', error);
          this.userLoadError = true;
        },
      });
  }

  private extractDisplayName(username?: string | null): string {
    if (!username) {
      return '';
    }
    const trimmed = username.trim();
    if (trimmed.includes('@')) {
      return trimmed.split('@')[0] || trimmed;
    }
    return trimmed;
  }

  private buildLernfelder(): Lernfeld[] {
    const focus = [
      'Grundlagen IT-Systeme',
      'Netzwerke und Dienste',
      'Programmierung Basics',
      'Web-Frontend',
      'Backend und APIs',
      'Datenbanken',
      'Security & Privacy',
      'Projektmanagement',
      'UX und Prototyping',
      'Cloud Grundlagen',
      'Automatisierung',
      'Testing & QA',
      'DevOps',
      'IT-Support',
      'Dokumentation',
    ];

    const items: Lernfeld[] = [];
    for (let i = 1; i <= 15; i++) {
      const assessments = this.createAssessments(i);
      const avg = this.weightedAverage(assessments);
      items.push({
        id: i,
        title: `Lernfeld ${i}`,
        focus: focus[i - 1] || 'Vertiefung',
        assessments,
        average: avg,
        trend: i % 3 === 0 ? '+2%' : '+1%',
      });
    }
    return items;
  }

  private createAssessments(seed: number): Assessment[] {
    const base = 1.7 + ((seed * 37) % 6) * 0.25;
    const mod = (offset: number) => Number(Math.min(5.5, base + offset).toFixed(1));
    return [
      { label: 'Test 1', grade: mod(0.0), weight: 0.35, type: 'Test' },
      { label: 'Test 2', grade: mod(0.3), weight: 0.25, type: 'Test' },
      { label: 'Projektarbeit', grade: mod(-0.2), weight: 0.25, type: 'Projekt' },
      { label: 'Mitarbeit', grade: mod(0.1), weight: 0.15, type: 'Mitarbeit' },
    ];
  }

  private weightedAverage(items: Assessment[]): number {
    const sum = items.reduce((acc, it) => acc + it.grade * it.weight, 0);
    return Number(sum.toFixed(1));
  }
}
