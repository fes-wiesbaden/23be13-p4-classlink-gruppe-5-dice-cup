import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { STUDENT_SERVICE, StudentService } from '../../features/student/student.service.contract';
import { StudentComponent } from './student';
import { StudentApiService } from '../../features/student/student.api.service';
import { fakeAsync, tick } from '@angular/core/testing';

describe('Student', () => {
  let component: StudentComponent;
  let fixture: ComponentFixture<StudentComponent>;
  let studentSvc: jasmine.SpyObj<StudentService>;
  let messageSvc: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    studentSvc = jasmine.createSpyObj<StudentApiService>('StudentApiService', ['loadCurrentStudent', 'loadProjects', 'loadScores']);
    studentSvc.loadCurrentStudent.and.returnValue(of({ id: 's1', name: 'Test', email: 't@test', className: '10A' }));
    studentSvc.loadProjects.and.returnValue(of([]));
    studentSvc.loadScores.and.returnValue(of(null));
    messageSvc = jasmine.createSpyObj<MessageService>('MessageService', ['add']);
    await TestBed.configureTestingModule({
      imports: [StudentComponent, HttpClientTestingModule],
    })
      .overrideProvider(StudentApiService, { useValue: studentSvc })
      .overrideProvider(MessageService, { useValue: messageSvc })
      .overrideComponent(StudentComponent, { set: { template: '<div></div>' } })
      .compileComponents();

    fixture = TestBed.createComponent(StudentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    studentSvc.loadCurrentStudent.calls.reset();
    studentSvc.loadProjects.calls.reset();
    studentSvc.loadScores.calls.reset();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('S1: current student load failure sets apiError and stops', fakeAsync(() => {
    const errSpy = spyOn(console, 'error').and.stub();
    studentSvc.loadCurrentStudent.and.returnValue(throwError(() => new Error('fail')));
    studentSvc.loadProjects.calls.reset();
    studentSvc.loadScores.calls.reset();

    const failFixture = TestBed.createComponent(StudentComponent);
    const failComponent = failFixture.componentInstance;
    failFixture.detectChanges();
    tick();
    failFixture.detectChanges();

    expect(failComponent['apiError']()).toContain('Profil konnte nicht geladen werden.');
    expect(studentSvc.loadProjects).not.toHaveBeenCalled();
    expect(studentSvc.loadScores).not.toHaveBeenCalled();
    errSpy.calls.reset();
  }));

  it('S2: no projects keeps averageGrade null and selectedProjectId null', () => {
    component.ngOnInit();
    fixture.detectChanges();
    expect(component['projects']().length).toBe(0);
    expect(component['selectedProjectId']()).toBeNull();
    expect(component['averageGrade']()).toBeNull();
    expect(component['openProjects']()).toBe(0);
  });

  it('current student success triggers project load', () => {
    const fx = TestBed.createComponent(StudentComponent);
    const cmp = fx.componentInstance;
    fx.detectChanges();
    expect(studentSvc.loadCurrentStudent).toHaveBeenCalled();
    expect(studentSvc.loadProjects).toHaveBeenCalledWith('s1');
  });

  it('projects set selectedProjectId and load scores', () => {
    studentSvc.loadProjects.and.returnValue(of([{ id: 'p1', name: 'Proj 1', projectGroupId: 'pg1', status: 'open' }]));
    studentSvc.loadScores.and.returnValue(of({ teacher: '2', peer: '3', self: '2', trendTeacher: '', trendPeer: '', trendSelf: '' }));
    const fx = TestBed.createComponent(StudentComponent);
    const cmp = fx.componentInstance;
    fx.detectChanges();
    expect(cmp['selectedProjectId']()).toBe('p1');
    expect(studentSvc.loadScores).toHaveBeenCalledWith('s1', 'pg1');
    expect(cmp['projects']()[0].scores?.teacher).toBe('2');
  });
});
