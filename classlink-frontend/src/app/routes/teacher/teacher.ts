// Von Lukas bearbeitet
import { Component, ViewEncapsulation, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { TeacherStudentSidebarComponent } from '../../features/teacher/components/student-sidebar/student-sidebar';
import { TeacherProjectAssignmentComponent } from '../../features/teacher/components/project-assignment/project-assignment';
import { TeacherMockService } from '../../features/teacher/mock.service';
import { Assignment, Project, ProjectOption, Student } from '../../features/teacher/models';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

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

interface EvaluationStatus {
  selfSubmitted: boolean;
  selfGrade: number | null;
  peerSubmitted: boolean;
  peerGrade: number | null;
}

@Component({
  standalone: true,
  selector: 'app-teacher',
  imports: [
    CommonModule,
    FormsModule,
    TeacherStudentSidebarComponent,
    TeacherProjectAssignmentComponent,
    Dialog,
    InputText,
    Toast,
  ],
  templateUrl: './teacher.html',
  styleUrls: ['./teacher.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService],
  encapsulation: ViewEncapsulation.None,
})
export class TeacherComponent {
  private readonly mock = inject(TeacherMockService);
  private readonly messages = inject(MessageService);


  readonly teacherName = 'F. Bekkaoui';
  readonly teacherSubject = 'Informatik';
  readonly teacherRole = 'Klassenlehrer 23BE13';

  constructor() {

    this.students = this.mock.getStudents();
    this.projects = this.mock.getProjects();
    this.classes = this.mock.getClasses();
    this.assignments = this.mock.getAssignments();
    this.projectOptions = this.projects.map((p) => ({ label: p.name, value: p.id }));
    this.selectedProjectId = this.projects[0]?.id ?? 0;
    this.currentProjectName = this.projects.find((p) => p.id === this.selectedProjectId)?.name;
    this.recomputeAssignedIds();
    
    const st = this.students.find((s) => s.id === this.selectedStudentId);
    this.assignClassName = st?.class ?? null;
    this.refreshLernfelderForStudent(this.selectedStudentId);
  }


  students: Student[] = [];
  projects: Project[] = [];
  assignments: Assignment[] = [];
  classes: string[] = [];

  projectOptions: ProjectOption[] = [];

  selectedStudentId = 1;
  selectedProjectId!: number;
  currentProjectName?: string;
  selectedClass: string | null = null;
  search = '';

  assignClassName: string | null = null;
  showCreateClass = false;
  newClassName = '';
  showSubjectDialog = false;
  subjectName = '';
  subjectDescription = '';
  lernfelder: Lernfeld[] = [];
  selectedLernfeld: Lernfeld | null = null;
  overallAverage = 0;
  runningProjects = 0;

  get selectedStudent() {
    return this.students.find((s) => s.id === this.selectedStudentId);
  }

  trackById(_: number, item: { id: number }) {
    return item.id;
  }

  get filteredStudents(): Student[] {
    const q = this.search.trim().toLowerCase();
    return this.students.filter(
      (s) =>
        (!this.selectedClass || s.class === this.selectedClass) &&
        (!q || s.name.toLowerCase().includes(q)),
    );
  }

  private recomputeAssignedIds() {
    this._assignedIds = this.assignments
      .filter((a) => a.projectId === this.selectedProjectId && a.assigned)
      .map((a) => a.studentId);
  }

  get evaluationStatus(): EvaluationStatus | null {
    const st = this.selectedStudent;
    if (!st) return null;
    return this.mock.getEvaluationStatus(st.id, this.selectedProjectId);
  }
  
  selectStudent(id: number) {
    this.selectedStudentId = id;

    const st = this.students.find((s) => s.id === id);
    this.assignClassName = st?.class ?? null;
    this.refreshLernfelderForStudent(id);
    this.refreshKpis();
  }

  isAssigned(studentId: number): boolean {
    const a = this.assignments.find(
      (x) => x.studentId === studentId && x.projectId === this.selectedProjectId,
    );
    return !!a?.assigned;
  }

  toggleAssignment(studentId: number) {
    this.mock.toggleAssignment(studentId, this.selectedProjectId);
    this.assignments = this.mock.getAssignments();
    const assigned = this.isAssigned(studentId);
    const student = this.students.find((s) => s.id === studentId)?.name || '#' + studentId;
    const project = this.currentProjectName || '';
    this.messages.add({
      severity: 'success',
      summary: assigned ? 'Zugewiesen' : 'Entfernt',
      detail: `${student} ${assigned ? 'zugeordnet zu' : 'entfernt von'} ${project}`,
    });
  }


  getScores(studentId: number, projectId: number) {
    return this.mock.getScores(studentId, projectId);
  }


  private _assignedIds: number[] = [];
  get assignedIds(): number[] {
    return this._assignedIds;
  }


  get scoreSummary() {
    const st = this.selectedStudent;
    if (!st) return null;
    return this.getScores(st.id, this.selectedProjectId);
  }


  onSelectStudent = (id: number) => this.selectStudent(id);
  onSearchChange = (v: string) => (this.search = v);
  onClassChange = (cls: string | null) => (this.selectedClass = cls);

  onProjectChange = (id: number) => {
    this.selectedProjectId = id;
    this.currentProjectName = this.projects.find((p) => p.id === id)?.name;
    this.recomputeAssignedIds();
    this.refreshKpis();
  };
  onToggleAssignment = (id: number) => this.toggleAssignment(id);
  onCreateProject = () => console.info('create project clicked');

  onCreateClass = () => {

    this.newClassName = '';
    this.showCreateClass = true;
  };


  get isNewClassValid(): boolean {
    const n = (this.newClassName || '').trim();
    if (!n) return false;
    if (this.classes.includes(n)) return false;
    return true;
  }

  confirmCreateClass() {
    const n = (this.newClassName || '').trim();
    if (!n || this.classes.includes(n)) return;
    this.mock.addClass(n);
    this.classes = this.mock.getClasses();
    this.selectedClass = n;
    this.assignClassName = n;
    this.showCreateClass = false;
    this.messages.add({ severity: 'success', summary: 'Klasse erstellt', detail: n });
  }

  cancelCreateClass() {
    this.showCreateClass = false;
    this.newClassName = '';
  }

  openSubjectDialog() {
    this.showSubjectDialog = true;
    this.subjectName = '';
    this.subjectDescription = '';
  }

  closeSubjectDialog() {
    this.showSubjectDialog = false;
  }

  saveSubject() {
    const name = this.subjectName.trim();
    const desc = this.subjectDescription.trim();
    if (!name || !desc) return;
    this.messages.add({
      severity: 'success',
      summary: 'Fach hinzugefügt',
      detail: `${name}: ${desc}`,
    });
    this.closeSubjectDialog();
  }


  onAssignSelectedToClass = () => {
    const st = this.selectedStudent;
    const cls = (this.assignClassName || '').trim();
    if (!st || !cls) return;
    this.mock.setStudentClass(st.id, cls);

    this.students = this.mock.getStudents();
    this.classes = this.mock.getClasses();

    if (this.selectedClass && this.selectedClass !== cls) {
      this.selectedClass = cls;
    }
    this.messages.add({
      severity: 'success',
      summary: 'Schueler zugewiesen',
      detail: `${st.name} -> ${cls}`,
    });
  };

  trackByLernfeld = (_: number, lf: Lernfeld) => lf.id;
  selectLernfeld(lf: Lernfeld) {
    this.selectedLernfeld = lf;
  }
  isLernfeldSelected(id: number): boolean {
    return this.selectedLernfeld?.id === id;
  }
  gradeColor(grade: number): string {
    if (grade <= 1.5) return '#d8f5ff';
    if (grade <= 2.5) return '#e5f3ff';
    if (grade <= 3.5) return '#fff4d5';
    if (grade <= 4.5) return '#ffe7d6';
    return '#ffdede';
  }

  gradeProgress(grade: number): number {
    const clamped = Math.min(6, Math.max(1, grade));
    const normalized = (6 - clamped) / 5;
    return Math.round(normalized * 100);
  }

  private refreshKpis() {
    if (!this.lernfelder.length) {
      this.overallAverage = 0;
    } else {
      const sum = this.lernfelder.reduce((acc, lf) => acc + lf.average, 0);
      this.overallAverage = Number((sum / this.lernfelder.length).toFixed(1));
    }
    this.runningProjects = this.assignments.filter(
      (a) => a.studentId === this.selectedStudentId && a.assigned,
    ).length;
  }

  private refreshLernfelderForStudent(studentId: number) {
    this.lernfelder = this.buildLernfelder(studentId);
    this.selectedLernfeld = this.lernfelder[0] ?? null;
    this.refreshKpis();
  }

  private buildLernfelder(seedOffset: number): Lernfeld[] {
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
    for (let i = 1; i <= 12; i++) {
      const assessments = this.createAssessments(i + seedOffset);
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

  onStartSelfEval() {
    this.messages.add({
      severity: 'info',
      summary: 'Selbst-Evaluation',
      detail: 'Evaluationsflow starten (noch Mock)',
    });
  }

  onStartPeerEval() {
    this.messages.add({
      severity: 'info',
      summary: 'Peer-Evaluation',
      detail: 'Evaluationsflow starten (noch Mock)',
    });
  }
}
