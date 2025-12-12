// Von Lukas bearbeitet
import { Component, ViewEncapsulation, ChangeDetectionStrategy, inject, signal, computed, effect, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TeacherStudentSidebarComponent } from '../../features/teacher/components/student-sidebar/student-sidebar';
import { TeacherProjectAssignmentComponent } from '../../features/teacher/components/project-assignment/project-assignment';
import { Assignment, ClassOption, Project, ProjectOption, Scores, Student } from '../../features/teacher/models';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TEACHER_SERVICE, TeacherService } from '../../features/teacher/teacher.service.contract';
import { TeacherApiService } from '../../features/teacher/teacher.api.service';
import { TeacherKpiCardsComponent } from '../../features/teacher/components/kpi-cards/kpi-cards';
import { TeacherHeaderBarComponent } from '../../features/teacher/components/header-bar/header-bar';

@Component({
  standalone: true,
  selector: 'app-teacher',
  imports: [
    CommonModule,
    FormsModule,
        TeacherStudentSidebarComponent,
        TeacherProjectAssignmentComponent,
        TeacherKpiCardsComponent,
        TeacherHeaderBarComponent,
        Toast,
  ],
  templateUrl: './teacher.html',
  styleUrls: ['./teacher.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    MessageService,
    { provide: TEACHER_SERVICE, useExisting: TeacherApiService },
  ],
  encapsulation: ViewEncapsulation.None,
})
export class TeacherComponent implements OnInit {
  private readonly teacher = inject<TeacherService>(TEACHER_SERVICE);
  private readonly messages = inject(MessageService);

  // Teacher identity (placeholder; could come from auth)
  readonly teacherName = 'F. Bekkaoui';
  readonly teacherSubject = 'Informatik';
  readonly teacherRole = 'Klassenlehrer 23BE13';

  students = signal<Student[]>([]);
  projects = signal<Project[]>([]);
  assignments = signal<Assignment[]>([]);
  classes = signal<ClassOption[]>([]);
  scores = signal<Scores | null>(null);

  search = signal('');
  selectedClassId = signal<string | null>(null);
  selectedStudentId = signal<string | null>(null);
  selectedProjectId = signal<string | null>(null);
  currentTermId = signal<string | null>(null);
  assignClassName = signal<string | null>(null);
  busy = signal(false);
  apiError = signal<string | null>(null);
  showCreateClass = signal(false);
  newClassName = signal('');

  projectOptions = computed<ProjectOption[]>(() => this.projects().map((p) => ({ label: p.name, value: p.id })));
  currentProjectName = computed(() => {
    const id = this.selectedProjectId();
    return this.projects().find((p) => p.id === id)?.name ?? '';
  });
  filteredStudents = computed(() => {
    const q = this.search().trim().toLowerCase();
    const cls = this.selectedClassId();
    return this.students().filter(
      (s) => (!cls || s.classId === cls) && (!q || s.name.toLowerCase().includes(q)),
    );
  });
  assignedIds = computed(() =>
    this.assignments()
      .filter((a) => a.projectId === this.selectedProjectId() && a.assigned)
      .map((a) => a.studentId),
  );
  selectedStudentView = computed(() => {
    const sid = this.selectedStudentId();
    if (!sid) return { name: '', className: '' };
    const st = this.students().find((s) => s.id === sid);
    return { name: st?.name ?? '', className: st?.className ?? '' };
  });
  scoreSummary = computed(() => this.scores());
  overallAverage = computed(() => {
    const sc = this.scores();
    if (!sc) return null;
    const vals = [sc.teacher, sc.peer, sc.self].map((v) => Number(String(v)));
    const valid = vals.filter((v) => !isNaN(v));
    if (!valid.length) return null;
    return Number((valid.reduce((acc, v) => acc + v, 0) / valid.length).toFixed(1));
  });
  runningProjects = computed(() =>
    this.assignments().filter((a) => a.studentId === this.selectedStudentId() && a.assigned).length,
  );

  constructor() {
    effect(() => {
      const sid = this.selectedStudentId();
      const pid = this.selectedProjectId();
      if (!sid || !pid) {
        this.scores.set(null);
        return;
      }
      this.teacher.loadScores(sid, pid).subscribe((res) => this.scores.set(res));
    });
  }

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll() {
    this.busy.set(true);
    this.apiError.set(null);
    this.teacher.loadContext().subscribe({
      next: (ctx) => {
        this.classes.set(ctx.classes);
        this.currentTermId.set(ctx.currentTermId);
        // Explicit selection required; do not auto-select class
        if (!ctx.classes.length) {
          this.selectedClassId.set(null);
          this.selectedProjectId.set(null);
          this.selectedStudentId.set(null);
          this.students.set([]);
          this.projects.set([]);
          this.assignments.set([]);
          return;
        }
        // keep class/project selection empty until user picks
      },
      error: (err) => {
        console.error('Failed to load teacher context', err);
        this.apiError.set('Lehrer-Kontext konnte nicht geladen werden.');
        this.busy.set(false);
      },
      complete: () => this.busy.set(false),
    });
  }

  private loadStudentsForClass(classId: string | null) {
    if (!classId) {
      this.students.set([]);
      this.selectedStudentId.set(null);
      return;
    }
    this.teacher.loadStudents(classId).subscribe((students) => {
      this.students.set(students);
      this.selectedStudentId.set(null);
      this.assignClassName.set(students[0]?.className ?? null);
    });
  }

  private loadProjectsForContext(classId: string | null, termId: string | null) {
    if (!classId) {
      this.projects.set([]);
      this.selectedProjectId.set(null);
      this.assignments.set([]);
      return;
    }
    this.teacher.loadProjects(classId, termId).subscribe((projects) => {
      this.projects.set(projects);
      if (!this.selectedProjectId() && projects.length) {
        this.selectedProjectId.set(projects[0].id);
      }
      const pid = this.selectedProjectId();
      if (pid) {
        this.loadAssignmentsForProject(pid);
      }
    });
  }

  private loadAssignmentsForProject(projectGroupId: string) {
    if (!projectGroupId) {
      this.assignments.set([]);
      return;
    }
    this.teacher.loadAssignments(projectGroupId).subscribe((assignments) => this.assignments.set(assignments));
  }

  onSelectStudent = (id: string) => {
    this.selectedStudentId.set(id);
    const st = this.students().find((s) => s.id === id);
    this.assignClassName.set(st?.className ?? null);
  };
  onSearchChange = (v: string) => this.search.set(v);
  onClassChange = (cls: string | null) => {
    this.selectedClassId.set(cls);
    this.selectedStudentId.set(null);
    this.selectedProjectId.set(null);
    this.projects.set([]);
    this.assignments.set([]);
    this.scores.set(null);
    if (cls) {
      this.loadStudentsForClass(cls);
      this.loadProjectsForContext(cls, this.currentTermId());
    }
  };
  onProjectChange = (id: string) => {
    this.selectedProjectId.set(id || null);
    if (id) {
      this.loadAssignmentsForProject(id);
    } else {
      this.assignments.set([]);
    }
  };

  onToggleAssignment = (id: string) => {
    const projectId = this.selectedProjectId();
    if (!projectId) {
      this.messages.add({ severity: 'warn', summary: 'Bitte zuerst ein Projekt auswählen.' });
      return;
    }
    const isAssigned = this.assignments().some((a) => a.projectId === projectId && a.studentId === id && a.assigned);
    const op$ = isAssigned
      ? this.teacher.removeStudentFromProject(projectId, id)
      : this.teacher.assignStudentToProject(projectId, id);
    op$.subscribe({
      next: () => {
        this.assignments.update((list) => {
          if (isAssigned) {
            return list.filter((a) => !(a.projectId === projectId && a.studentId === id));
          }
          return [...list, { studentId: id, projectId, assigned: true }];
        });
        this.messages.add({
          severity: 'success',
          summary: isAssigned ? 'Entfernt' : 'Zugewiesen',
          detail: `Schüler ${id}`,
        });
      },
      error: (err) => {
        console.error('Assignment update failed', err);
        this.messages.add({ severity: 'error', summary: 'Fehler', detail: 'Zuweisung fehlgeschlagen' });
      },
    });
  };
  onCreateProject = () => console.info('create project clicked');
  onCreateClass = () => {
    this.newClassName.set('');
    this.showCreateClass.set(true);
  };

  get isNewClassValid(): boolean {
    const n = (this.newClassName() || '').trim();
    if (!n) return false;
    if (this.classes().some((c) => c.name === n)) return false;
    return true;
  }
  confirmCreateClass() {
    const n = (this.newClassName() || '').trim();
    if (!n || this.classes().some((c) => c.name === n)) return;
    const newCls: ClassOption = { id: n, name: n };
    this.classes.update((arr) => [...arr, newCls]);
    this.selectedClassId.set(newCls.id);
    this.assignClassName.set(n);
    this.showCreateClass.set(false);
    this.messages.add({ severity: 'success', summary: 'Klasse erstellt', detail: n });
  }
  cancelCreateClass() {
    this.showCreateClass.set(false);
    this.newClassName.set('');
  }

  onAssignSelectedToClass = () => {
    const stId = this.selectedStudentId();
    const cls = (this.assignClassName() || '').trim();
    if (!stId || !cls) return;
    const newCls: ClassOption = { id: cls, name: cls };
    this.classes.update((c) => (c.some((x) => x.id === cls) ? c : [...c, newCls]));
    this.students.update((list) =>
      list.map((s) => (s.id === stId ? { ...s, className: cls } : s)),
    );
    if (this.selectedClassId() && this.selectedClassId() !== cls) {
      this.selectedClassId.set(cls);
    }
    this.messages.add({
      severity: 'success',
      summary: 'Schueler zugewiesen',
      detail: `${stId} -> ${cls}`,
    });
  };
}
