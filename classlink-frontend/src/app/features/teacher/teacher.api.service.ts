import { Injectable } from '@angular/core';
import { Observable, map, of, switchMap, tap } from 'rxjs';
import { UserControllerService } from '../../api';
import { ClassControllerService } from '../../api/api/class-controller.service';
import { ProjectGroupControllerService } from '../../api/api/project-group-controller.service';
import { ProjectGroupMembershipControllerService } from '../../api/api/project-group-membership-controller.service';
import { ProjectGroupScoreControllerService } from '../../api/api/project-group-score-controller.service';
import { TeacherContextControllerService } from '../../api/api/teacher-context-controller.service';
import { TeacherService } from './teacher.service.contract';
import { Assignment, ClassOption, Project, Scores, Student } from './models';
import { TeacherContextDto } from '../../api/model/teacher-context-dto';
import { ProjectGroupStudentScoreOverviewDto } from '../../api/model/project-group-student-score-overview-dto';

@Injectable({ providedIn: 'root' })
export class TeacherApiService implements TeacherService {
  constructor(
    private readonly userApi: UserControllerService,
    private readonly classApi: ClassControllerService,
    private readonly teacherContextApi: TeacherContextControllerService,
    private readonly projectGroupApi: ProjectGroupControllerService,
    private readonly membershipApi: ProjectGroupMembershipControllerService,
    private readonly scoreApi: ProjectGroupScoreControllerService,
  ) {}

  loadContext(): Observable<{ classes: ClassOption[]; currentClassId: string | null; currentTermId: string | null }> {
    return this.teacherContextApi.getContext().pipe(
      map((ctx: TeacherContextDto) => {
        const classes = (ctx.classes ?? []).map((c) => ({ id: c.classId ?? '', name: c.className ?? '' }));
        const firstClass = ctx.classes?.[0];
        const currentTerm = firstClass?.terms?.find((t) => t.isCurrent) ?? firstClass?.terms?.[0];
        return {
          classes,
          currentClassId: firstClass?.classId ?? null,
          currentTermId: currentTerm?.termId ?? null,
        };
      }),
      tap((ctx) => console.log('[TeacherApiService] context', ctx)),
    );
  }

  loadStudents(classId: string | null): Observable<Student[]> {
    if (!classId) {
      return of([] as Student[]);
    }
    return this.classApi.listClassStudents(classId).pipe(
      map((dtos) =>
        dtos.map((u) => ({
          id: u.studentId ?? '',
          name: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim() || 'Unbekannt',
          classId: u.classId ?? '',
          className: u.className ?? '',
          avatarUrl: undefined,
        })),
      ),
      tap((students) => console.log('[TeacherApiService] mapped students', students)),
    );
  }

  loadClasses(): Observable<string[]> {
    return this.classApi.list2().pipe(
      map((classes) => classes.map((c) => c.name || '').filter(Boolean)),
      tap((classes) => console.log('[TeacherApiService] classes', classes)),
    );
  }

  loadProjects(classId: string | null, termId: string | null): Observable<Project[]> {
    if (!classId) return of([] as Project[]);
    // Teacher context already contains project groups; fetch via teacher context again to keep it in sync
    return this.teacherContextApi.getContext().pipe(
      map((ctx) => {
        const cls = ctx.classes?.find((c) => c.classId === classId);
        const term =
          (termId && cls?.terms?.find((t) => t.termId === termId)) ||
          cls?.terms?.find((t) => t.isCurrent) ||
          cls?.terms?.[0];
        const groups = term?.projectGroups ?? [];
        return groups.map((g) => ({
          id: g.projectGroupId ?? '',
          name: g.projectName ?? 'Projekt',
        }));
      }),
      tap((projects) => console.log('[TeacherApiService] projects (project groups)', projects)),
    );
  }

  loadAssignments(projectGroupId: string): Observable<Assignment[]> {
    if (!projectGroupId) return of([] as Assignment[]);
    return this.scoreApi.listScores(projectGroupId).pipe(
      map((items: ProjectGroupStudentScoreOverviewDto[]) =>
        items
          .filter((it) => !!it.studentId)
          .map((it) => ({
            studentId: it.studentId!,
            projectId: projectGroupId,
            assigned: true,
          })),
      ),
      tap((assignments) => console.log('[TeacherApiService] assignments', assignments)),
    );
  }

  assignStudentToProject(projectGroupId: string, studentId: string): Observable<void> {
    return this.membershipApi.addMember(projectGroupId, studentId).pipe(map(() => void 0));
  }

  removeStudentFromProject(projectGroupId: string, studentId: string): Observable<void> {
    return this.membershipApi.removeMember(projectGroupId, studentId).pipe(map(() => void 0));
  }

  loadScores(studentId: string, projectGroupId: string): Observable<Scores | null> {
    if (!projectGroupId || !studentId) return of(null);
    return this.scoreApi.listScores(projectGroupId).pipe(
      map((items: ProjectGroupStudentScoreOverviewDto[]) => {
        const entry = items.find((it) => it.studentId === studentId);
        if (!entry) return null;
        const fmt = (n?: number | null) => (typeof n === 'number' ? n.toFixed(1) : '0');
        return {
          teacher: fmt(entry.teacherScore),
          peer: fmt(entry.peerScore),
          self: fmt(entry.selfScore),
          trendTeacher: '',
          trendPeer: '',
          trendSelf: '',
        };
      }),
      tap((scores) => console.log('[TeacherApiService] scores', scores)),
    );
  }
}
