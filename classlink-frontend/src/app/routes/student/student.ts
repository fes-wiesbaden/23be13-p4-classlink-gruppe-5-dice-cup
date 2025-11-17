import { Component, ViewEncapsulation, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Card } from 'primeng/card';
import { Chart, registerables } from 'chart.js';
import DataLabelsPlugin from 'chartjs-plugin-datalabels';
import { ChartModule } from 'primeng/chart';
import { TeacherMockService } from '../../features/teacher/mock.service';
import { AuthService } from '../../../services/auth.service';
import { Assignment, Project, ProjectOption, Student } from '../../features/teacher/models';
import { TeacherKpiCardsComponent } from '../../features/teacher/components/kpi-cards/kpi-cards';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  standalone: true,
  selector: 'app-student',
  imports: [CommonModule, FormsModule, Card, TeacherKpiCardsComponent, ChartModule, Toast],
  templateUrl: './student.html',
  styleUrl: './student.scss',
  providers: [MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class StudentComponent {
  constructor(private readonly mock: TeacherMockService, private readonly auth: AuthService, private readonly messages: MessageService) {
    // Register Chart.js + plugins once
    try { Chart.register(...registerables, DataLabelsPlugin); } catch {}
    this.students = this.mock.getStudents();
    this.projects = this.mock.getProjects();
    this.assignments = this.mock.getAssignments();
    this.projectOptions = this.projects.map(p => ({ label: p.name, value: p.id }));
    this.selectedProjectId = this.projects[0]?.id ?? 0;
    this.currentProjectName = this.projects.find(p => p.id === this.selectedProjectId)?.name;
    this.updateChartDerived();

    // derive current student id from login username
    const uname = (this.auth.getUsername() || '').trim();
    if (uname) this.currentStudentId = this.pickStudentIdFor(uname);
  }

  // Mock current student id (dev)
  currentStudentId = 1;

  // Data
  students: Student[] = [];
  projects: Project[] = [];
  assignments: Assignment[] = [];
  projectOptions: ProjectOption[] = [];

  selectedProjectId!: number;
  currentProjectName?: string;

  // Peer evaluation form
  peerTargetId: number | null = null;
  peerGrade: number = 2.0;
  busy = false;

  get currentStudent() {
    return this.students.find(s => s.id === this.currentStudentId);
  }

  private pickStudentIdFor(username: string): number {
    // Try exact name match (case-insensitive)
    const byName = this.students.find(s => s.name.toLowerCase() === username.toLowerCase());
    if (byName) return byName.id;
    // Deterministic fallback: hash username to one of the students
    const n = this.students.length || 1;
    const hash = Array.from(username).reduce((acc, ch) => (acc * 31 + ch.charCodeAt(0)) >>> 0, 7);
    const idx = hash % n;
    return this.students[idx].id;
  }

  onProjectChange = (id: number) => {
    this.selectedProjectId = id;
    this.currentProjectName = this.projects.find(p => p.id === id)?.name;
    this.updateChartDerived();
  };

  get peers(): Student[] {
    return this.students.filter(s => s.id !== this.currentStudentId);
  }

  get scoreSummary() {
    return this.mock.getScores(this.currentStudentId, this.selectedProjectId);
  }

  get notes() {
    return this.mock.getNotesFor(this.currentStudentId, this.selectedProjectId);
  }

  // Grouped bar chart (Performance = 7 - Note)
  chartData: any;
  private updateChartDerived() {
    const hist = this.mock.getScoreHistory(this.currentStudentId, this.selectedProjectId, 8);
    const perf = (arr: number[]) => arr.map(v => Number((7 - v).toFixed(1)));
    this.chartData = {
      labels: hist.labels,
      datasets: [
        {
          label: 'Lehrer',
          data: perf(hist.teacher),
          // @ts-ignore store original note for tooltip
          orig: hist.teacher,
          backgroundColor: 'rgba(139, 92, 246, 0.6)',
          borderColor: 'rgba(139, 92, 246, 0.9)',
          borderWidth: 1,
          borderRadius: 6,
          maxBarThickness: 42,
        },
        {
          label: 'Fremd',
          data: perf(hist.peer),
          // @ts-ignore
          orig: hist.peer,
          backgroundColor: 'rgba(34, 197, 94, 0.55)',
          borderColor: 'rgba(34, 197, 94, 0.9)',
          borderWidth: 1,
          borderRadius: 6,
          maxBarThickness: 42,
        },
        {
          label: 'Selbst',
          data: perf(hist.self),
          // @ts-ignore
          orig: hist.self,
          backgroundColor: 'rgba(59, 130, 246, 0.55)',
          borderColor: 'rgba(59, 130, 246, 0.9)',
          borderWidth: 1,
          borderRadius: 6,
          maxBarThickness: 42,
        },
      ],
    };
  }

  chartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary') || '#e8eaed',
        },
      },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: (ctx: any) => {
            const ds: any = ctx.dataset || {};
            const perf = typeof ctx.raw === 'number' ? ctx.raw : Number(ctx.raw);
            const orig = ds.orig?.[ctx.dataIndex];
            const name = ds.label || 'Wert';
            const perfTxt = perf.toFixed(1);
            const origTxt = orig != null ? Number(orig).toFixed(1) : undefined;
            return origTxt ? `${name}: Leistung ${perfTxt} (Note ${origTxt})` : `${name}: Leistung ${perfTxt}`;
          },
        },
      },
      datalabels: {
        anchor: 'end',
        align: 'end',
        clamp: true,
        offset: 2,
        color: '#e8eaed',
        borderRadius: 4,
        backgroundColor: (ctx: any) => {
          // subtle backdrop to enhance readability
          return 'rgba(0,0,0,0.28)';
        },
        padding: { top: 2, bottom: 2, left: 6, right: 6 },
        formatter: (value: number, ctx: any) => {
          const ds: any = ctx.dataset || {};
          const orig = ds.orig?.[ctx.dataIndex];
          return (orig != null ? Number(orig).toFixed(1) : Number(value).toFixed(1));
        },
      },
    },
    interaction: { mode: 'index', intersect: false },
    scales: {
      x: {
        stacked: false,
        ticks: {
          color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#9aa0a6',
        },
        grid: { color: 'rgba(255,255,255,0.06)' },
      },
      y: {
        min: 0,
        max: 6,
        stacked: false,
        title: {
          display: true,
          text: 'Leistung (höher = besser)',
          color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#9aa0a6',
        },
        ticks: {
          stepSize: 1,
          color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#9aa0a6',
        },
        grid: { color: 'rgba(255,255,255,0.06)' },
      },
    },
  };

  canSubmitPeer(): boolean {
    return !!this.peerTargetId && this.peerTargetId !== this.currentStudentId && this.peerGrade >= 1 && this.peerGrade <= 6;
  }

  submitPeerEvaluation() {
    if (!this.canSubmitPeer() || this.peerTargetId == null) return;
    this.busy = true;
    this.mock.submitPeerEvaluation(this.currentStudentId, this.peerTargetId, this.selectedProjectId, Number(this.peerGrade));
    console.info('Peer evaluation submitted', {
      from: this.currentStudentId,
      to: this.peerTargetId,
      projectId: this.selectedProjectId,
      grade: this.peerGrade,
    });
    // reset selection lightly
    this.peerTargetId = null;
    this.peerGrade = 2.0;
    this.messages.add({ severity: 'success', summary: 'Danke!', detail: 'Fremdbewertung wurde übermittelt.' });
    this.busy = false;
  }

  // trackBy helpers
  trackById(_: number, item: { id: number }) { return item.id; }
  trackByIndex(i: number) { return i; }
}




