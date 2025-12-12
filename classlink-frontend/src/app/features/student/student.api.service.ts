import { Injectable, inject } from '@angular/core';
import { Observable, combineLatest, map, of, switchMap, tap } from 'rxjs';
import { UserControllerService } from '../../api';
import { ProjectControllerService } from '../../api/api/project-controller.service';
import { TermControllerService } from '../../api/api/term-controller.service';
import { ProjectGroupControllerService } from '../../api/api/project-group-controller.service';
import { ProjectGroupScoreControllerService } from '../../api/api/project-group-score-controller.service';
import { Scores } from '../teacher/models';
import { CurrentStudent, StudentProject, StudentService } from './student.service.contract';
import { ProjectGroupStudentScoreOverviewDto } from '../../api/model/project-group-student-score-overview-dto';

@Injectable({ providedIn: 'root' })
export class StudentApiService implements StudentService {
  private readonly userApi = inject(UserControllerService);
  private readonly projectApi = inject(ProjectControllerService);
  private readonly termApi = inject(TermControllerService);
  private readonly projectGroupApi = inject(ProjectGroupControllerService);
  private readonly scoreApi = inject(ProjectGroupScoreControllerService);

  // TEMP: derive current student from generic users API until backend exposes a dedicated /me endpoint.
  loadCurrentStudent(): Observable<CurrentStudent | null> {
    return this.userApi.getUsers().pipe(
      map((users) => users.find((u) => u.role === 'STUDENT') ?? users[0] ?? null),
      map((u) =>
        u
          ? {
              id: u.id ?? '',
              name: `${u.userInfo?.firstName ?? ''} ${u.userInfo?.lastName ?? ''}`.trim() || u.username || 'Student',
              email: u.userInfo?.email || u.username || 'student@dicecup.local',
              className: (u as any)?.className,
            }
          : null,
      ),
      tap((profile) => console.log('[StudentApiService] profile', profile)),
    );
  }

  loadProjects(studentId: string): Observable<StudentProject[]> {
    return this.termApi.list3(undefined, 'OPEN').pipe(
      switchMap((terms) => {
        const termId = terms[0]?.id;
        if (!termId) return of([] as StudentProject[]);
        return this.projectApi.listProjectsForStudent(studentId, termId).pipe(
          switchMap((projects) =>
            projects.length
              ? this.mapProjectsWithGroups(projects)
              : of([] as StudentProject[]),
          ),
        );
      }),
      tap((projects) => console.log('[StudentApiService] projects', projects)),
    );
  }

  private mapProjectsWithGroups(projects: any[]): Observable<StudentProject[]> {
    // naive approach: take first group per project if exists
    const mapped$ = projects.map((p) =>
      this.projectGroupApi.list1(p.id ?? '').pipe(
        map((groups) => {
          const groupId = groups[0]?.id ?? '';
          return {
            id: p.id ?? '',
            name: p.name ?? 'Projekt',
            projectGroupId: groupId,
            status: p.active === false ? 'done' : 'open',
            grade: undefined,
          } as StudentProject;
        }),
      ),
    );
    return mapped$.length ? combineLatest(mapped$) : of([]);
  }

  loadScores(studentId: string, projectGroupId: string): Observable<Scores | null> {
    if (!projectGroupId) return of(null);
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
      tap((scores) => console.log('[StudentApiService] scores', scores)),
    );
  }
}
