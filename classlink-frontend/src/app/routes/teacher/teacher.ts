// Von Lukas bearbeitet
import { Component, ViewEncapsulation, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { TeacherStudentSidebarComponent } from '../../features/teacher/components/student-sidebar/student-sidebar';
import { TeacherHeaderBarComponent } from '../../features/teacher/components/header-bar/header-bar';
import { TeacherKpiCardsComponent } from '../../features/teacher/components/kpi-cards/kpi-cards';
import { TeacherNoteFormComponent } from '../../features/teacher/components/note-form/note-form';
import { TeacherProjectAssignmentComponent } from '../../features/teacher/components/project-assignment/project-assignment';
import { TeacherMockService } from '../../features/teacher/mock.service';
import { Assignment, Project, ProjectOption, Student } from '../../features/teacher/models';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  standalone: true,
  selector: 'app-teacher',
  imports: [
    CommonModule,
    FormsModule,
    Dialog,
    InputText,
    TeacherStudentSidebarComponent,
    TeacherHeaderBarComponent,
    TeacherKpiCardsComponent,
    TeacherNoteFormComponent,
    TeacherProjectAssignmentComponent,
    Toast,
  ],
  templateUrl: './teacher.html',
  styleUrl: './teacher.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService],
  encapsulation: ViewEncapsulation.None,
})
export class TeacherComponent {
  constructor(
    private readonly mock: TeacherMockService,
    private readonly messages: MessageService,
  ) {
    // Daten aus dem Mock holen und Ansicht vorbereiten
    this.students = this.mock.getStudents();
    this.projects = this.mock.getProjects();
    this.classes = this.mock.getClasses();
    this.assignments = this.mock.getAssignments();
    this.projectOptions = this.projects.map((p) => ({ label: p.name, value: p.id }));
    this.selectedProjectId = this.projects[0]?.id ?? 0;
    this.currentProjectName = this.projects.find((p) => p.id === this.selectedProjectId)?.name;
    this.recomputeAssignedIds();
    // Ausgewaehlte Klasse im kleinen Zuweisungsfeld (links in der Sidebar)
    const st = this.students.find((s) => s.id === this.selectedStudentId);
    this.assignClassName = st?.class ?? null;
  }

  // Daten (kommen aus dem Mock)
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
  noteText = '';
  // Ausgewaehlte Klasse im kleinen Zuweisungsfeld (links in der Sidebar)
  assignClassName: string | null = null;
  showCreateClass = false;
  newClassName = '';

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
  // Wenn links ein Schüler angeklickt wird: merken und Klasse vorbelegen
  selectStudent(id: number) {
    this.selectedStudentId = id;
    // Preselect current class for assignment control
    const st = this.students.find((s) => s.id === id);
    this.assignClassName = st?.class ?? null;
  }

  isAssigned(studentId: number): boolean {
    const a = this.assignments.find(
      (x) => x.studentId === studentId && x.projectId === this.selectedProjectId,
    );
    return !!a?.assigned;
  }

  // Zuweisung umschalten und kurze Rückmeldung anzeigen
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

  // Noten aus dem Mock holen
  getScores(studentId: number, projectId: number) {
    return this.mock.getScores(studentId, projectId);
  }

  sendNote() {
    if (!this.noteText.trim()) return;
    const to = this.selectedStudent;
    if (to) {
      this.mock.addNote(to.id, this.selectedProjectId, this.noteText.trim());
    }
    this.noteText = '';
    this.messages.add({
      severity: 'success',
      summary: 'Nachricht gesendet',
      detail: to ? `An ${to.name}` : '',
    });
  }

  // Derived data for presentational components
  private _assignedIds: number[] = [];
  get assignedIds(): number[] {
    return this._assignedIds;
  }

  // Daten fuer die KPI-Karten oben
  get scoreSummary() {
    const st = this.selectedStudent;
    if (!st) return null;
    return this.getScores(st.id, this.selectedProjectId);
  }

  // Ereignisse aus den Kind-Komponenten
  onSelectStudent = (id: number) => this.selectStudent(id);
  onSearchChange = (v: string) => (this.search = v);
  onClassChange = (cls: string | null) => (this.selectedClass = cls);
  // Beim Projektwechsel Namen und Zuweisungen aktualisieren
  onProjectChange = (id: number) => {
    this.selectedProjectId = id;
    this.currentProjectName = this.projects.find((p) => p.id === id)?.name;
    this.recomputeAssignedIds();
  };
  onToggleAssignment = (id: number) => this.toggleAssignment(id);
  onSendNote = () => this.sendNote();
  onCreateProject = () => console.info('create project clicked');
  // Ã–ffnet den Dialog zum Anlegen einer neuen Klasse
  onCreateClass = () => {
    // Dialog statt prompt öffnen
    this.newClassName = '';
    this.showCreateClass = true;
  };

  // Kleine Validierung: leer oder schon vorhanden -> nicht erlauben
  get isNewClassValid(): boolean {
    const n = (this.newClassName || '').trim();
    if (!n) return false;
    if (this.classes.includes(n)) return false;
    return true;
  }
  // Bestätigt den Dialog und legt die Klasse an
  confirmCreateClass() {
    const n = (this.newClassName || '').trim();
    if (!n || this.classes.includes(n)) return;
    this.mock.addClass(n);
    this.classes = this.mock.getClasses();
    this.selectedClass = n;
    this.showCreateClass = false;
    this.messages.add({ severity: 'success', summary: 'Klasse erstellt', detail: n });
  }
  // Dialog einfach schließen ohne zu speichern
  cancelCreateClass() {
    this.showCreateClass = false;
    this.newClassName = '';
  }

  // Weist den aktuell ausgewÃ¤hlten SchÃ¼ler der im Dropdown gewÃ¤hlten Klasse zu
  // Weist den aktuell ausgewaehlten Schueler der gewaehlten Klasse zu
  onAssignSelectedToClass = () => {
    const st = this.selectedStudent;
    const cls = (this.assignClassName || '').trim();
    if (!st || !cls) return;
    this.mock.setStudentClass(st.id, cls);
    // lokale Kopien aktualisieren
    this.students = this.mock.getStudents();
    this.classes = this.mock.getClasses();
    // Filter anpassen, falls notwendig
    if (this.selectedClass && this.selectedClass !== cls) {
      this.selectedClass = cls;
    }
    this.messages.add({
      severity: 'success',
      summary: 'Schueler zugewiesen',
      detail: `${st.name} -> ${cls}`,
    });
  };
}
