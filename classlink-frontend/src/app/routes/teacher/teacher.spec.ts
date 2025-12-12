// Von Lukas bearbeitet
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { TEACHER_SERVICE, TeacherService } from '../../features/teacher/teacher.service.contract';
import { TeacherComponent } from './teacher';
import { MessageService } from 'primeng/api';
import { TeacherApiService } from '../../features/teacher/teacher.api.service';

describe('Teacher', () => {
  let component: TeacherComponent;
  let fixture: ComponentFixture<TeacherComponent>;
  let teacherSvc: jasmine.SpyObj<TeacherApiService>;

  beforeEach(async () => {
    teacherSvc = jasmine.createSpyObj<TeacherApiService>('TeacherApiService', [
      'loadContext',
      'loadStudents',
      'loadProjects',
      'loadAssignments',
      'loadScores',
      'assignStudentToProject',
      'removeStudentFromProject',
    ]);
    teacherSvc.loadContext.and.returnValue(of({ classes: [], currentClassId: null, currentTermId: null }));
    teacherSvc.loadStudents.and.returnValue(of([]));
    teacherSvc.loadProjects.and.returnValue(of([]));
    teacherSvc.loadAssignments.and.returnValue(of([]));
    teacherSvc.loadScores.and.returnValue(of(null));
    await TestBed.configureTestingModule({
      imports: [TeacherComponent],
    })
      .overrideProvider(TeacherApiService, { useValue: teacherSvc })
      .overrideComponent(TeacherComponent, { set: { template: '<div></div>' } })
      .compileComponents();

    fixture = TestBed.createComponent(TeacherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('T1: sets apiError and stops on context failure', () => {
    const errSpy = spyOn(console, 'error').and.stub();
    teacherSvc.loadContext.and.returnValue(throwError(() => new Error('fail')));
    fixture = TestBed.createComponent(TeacherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component['apiError']()).toContain('Lehrer-Kontext');
    expect(teacherSvc.loadStudents).not.toHaveBeenCalled();
    expect(teacherSvc.loadProjects).not.toHaveBeenCalled();
    errSpy.calls.reset();
  });

  it('T3/T4: does not auto-select class or project when context has classes but none selected', () => {
    teacherSvc.loadContext.and.returnValue(of({ classes: [{ id: 'c1', name: '10A' }], currentClassId: 'c1', currentTermId: 't1' }));
    fixture = TestBed.createComponent(TeacherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component['selectedClassId']()).toBeNull();
    expect(teacherSvc.loadStudents).not.toHaveBeenCalled();
    expect(teacherSvc.loadProjects).not.toHaveBeenCalled();
  });

  it('T5: toggling without project shows no call', () => {
    fixture = TestBed.createComponent(TeacherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.onToggleAssignment('student-1');
    expect(teacherSvc.assignStudentToProject).not.toHaveBeenCalled();
    expect(teacherSvc.removeStudentFromProject).not.toHaveBeenCalled();
  });

  it('class change loads students and projects and resets selections', () => {
    teacherSvc.loadContext.and.returnValue(of({
      classes: [{ id: 'c1', name: '10A', terms: [{ termId: 't1', isCurrent: true }] }],
      currentClassId: 'c1',
      currentTermId: 't1',
    }));
    teacherSvc.loadStudents.calls.reset();
    teacherSvc.loadProjects.calls.reset();

    const fx = TestBed.createComponent(TeacherComponent);
    const cmp = fx.componentInstance;
    fx.detectChanges();

    cmp.onClassChange('c1');
    expect(teacherSvc.loadStudents).toHaveBeenCalledWith('c1');
    expect(teacherSvc.loadProjects).toHaveBeenCalledWith('c1', 't1');
    expect(cmp['selectedStudentId']()).toBeNull();
    expect(cmp['selectedProjectId']()).toBeNull();
  });

  it('project change loads assignments', () => {
    teacherSvc.loadAssignments.calls.reset();
    const fx = TestBed.createComponent(TeacherComponent);
    const cmp = fx.componentInstance;
    fx.detectChanges();

    cmp.onProjectChange('pg1');
    expect(teacherSvc.loadAssignments).toHaveBeenCalledWith('pg1');
  });

  it('toggle assignment success updates assignments signal', () => {
    teacherSvc.assignStudentToProject.and.returnValue(of(void 0));
    const fx = TestBed.createComponent(TeacherComponent);
    const cmp = fx.componentInstance;
    fx.detectChanges();
    cmp['selectedProjectId'].set('pg1');
    cmp['assignments'].set([]);

    cmp.onToggleAssignment('s1');
    expect(cmp['assignments']()).toEqual([{ studentId: 's1', projectId: 'pg1', assigned: true }]);
  });

  it('toggle assignment failure does not mutate assignments', () => {
    const errSpy = spyOn(console, 'error').and.stub();
    teacherSvc.assignStudentToProject.and.returnValue(throwError(() => new Error('fail')));
    const fx = TestBed.createComponent(TeacherComponent);
    const cmp = fx.componentInstance;
    fx.detectChanges();
    cmp['selectedProjectId'].set('pg1');
    cmp['assignments'].set([]);

    cmp.onToggleAssignment('s1');
    expect(cmp['assignments']()).toEqual([]);
    errSpy.calls.reset();
  });
});
