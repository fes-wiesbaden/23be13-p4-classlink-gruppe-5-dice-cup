import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { GradeHistoryStripComponent } from '../../features/student/components/grade-history/grade-history';
import { AuthService } from '../../../services/auth.service';
import { TeacherMockService } from '../../features/teacher/mock.service';
import { Scores } from '../../features/teacher/models';

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
  peerCount: number;
  selfDone: boolean;
  peerDone: boolean;
  color: string;
}

@Component({
  standalone: true,
  selector: 'app-student',
  imports: [CommonModule, Button, Toast, GradeHistoryStripComponent],
  templateUrl: './student.html',
  styleUrl: './student.scss',
  providers: [MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudentComponent {
  readonly studentId = 1;
  studentName: string;
  studentClass = '10A';
  lernfelder: Lernfeld[] = [];
  projects: StudentProject[] = [];
  averageGrade = 0;
  history: { labels: string[]; teacher: number[]; peer: number[]; self: number[] } | null = null;
  historyProjectName = '';
  selectedLernfeld: Lernfeld | null = null;

  constructor(
    private readonly auth: AuthService,
    private readonly mock: TeacherMockService,
    private readonly messages: MessageService,
  ) {
    this.studentName = this.auth.getUsername() || 'Anna Schmidt';
    const cls = this.mock.getStudents().find(s => s.id === this.studentId)?.class;
    if (cls) {
      this.studentClass = cls;
    }
    this.lernfelder = this.buildLernfelder();
    this.averageGrade = Number(
      (this.lernfelder.reduce((acc, lf) => acc + lf.average, 0) / this.lernfelder.length).toFixed(1),
    );
    this.projects = this.buildProjects();
    if (this.projects.length) {
      const first = this.projects[0];
      this.history = this.mock.getScoreHistory(this.studentId, first.id, 6);
      this.historyProjectName = first.name;
    }
    this.selectedLernfeld = this.lernfelder[0] ?? null;
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

  startSelfEvaluation(project: StudentProject): void {
    project.selfDone = true;
    this.messages.add({
      severity: 'info',
      summary: 'Selbst-Evaluation',
      detail: `${project.name} vorbereitet.`,
    });
  }

  startPeerEvaluation(project: StudentProject): void {
    project.peerDone = true;
    this.messages.add({
      severity: 'success',
      summary: 'Peer-Evaluation',
      detail: `${project.name}: teile dein Feedback.`,
    });
  }

  gradeColor(grade: number): string {
    if (grade <= 1.5) return '#c7f6d9';
    if (grade <= 2.5) return '#d7f0ff';
    if (grade <= 3.5) return '#ffeec7';
    if (grade <= 4.5) return '#ffe0c2';
    return '#ffd5d5';
  }

  private buildProjects(): StudentProject[] {
    const assignments = this.mock.getAssignments().filter(a => a.studentId === this.studentId && a.assigned);
    const projects = this.mock.getProjects();
    const assignedProjects = assignments.length
      ? assignments
          .map(a => projects.find(p => p.id === a.projectId))
          .filter((p): p is NonNullable<typeof p> => !!p)
      : projects.slice(0, 2);

    const palette = ['linear-gradient(120deg, #7c3aed, #38bdf8)', 'linear-gradient(120deg, #22c55e, #4f46e5)', 'linear-gradient(120deg, #f59e0b, #6366f1)'];

    return assignedProjects.map((p, idx) => {
      const scores = this.mock.getScores(this.studentId, p.id);
      return {
        id: p.id,
        name: p.name,
        role: ['Team Delta', 'Team Nova', 'Team Aurora'][idx % 3],
        nextDue: ['12.12.', '18.12.', '08.01.'][idx % 3],
        progress: 65 + (idx * 11) % 25,
        scores,
        peerCount: 2 + (idx % 3),
        selfDone: idx === 0,
        peerDone: false,
        color: palette[idx % palette.length],
      };
    });
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
