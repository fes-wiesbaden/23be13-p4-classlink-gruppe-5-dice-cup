import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { AuthControllerService, TokenResponse } from '../app/api';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

const futureExp = Math.floor(Date.now() / 1000) + 3600;
const pastExp = Math.floor(Date.now() / 1000) - 3600;

function createJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

describe('AuthService', () => {
  let service: AuthService;
  let authApi: jasmine.SpyObj<AuthControllerService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authApi = jasmine.createSpyObj('AuthControllerService', ['login', 'refresh']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthControllerService, useValue: authApi },
        { provide: Router, useValue: router },
      ],
    });

    service = TestBed.inject(AuthService);
    localStorage.clear();
  });

  it('stores tokens and roles on login', (done) => {
    const token = createJwt({ exp: futureExp, roles: ['ROLE_ADMIN'] });
    const response: TokenResponse = { accessToken: token, refreshToken: 'refresh-1' };
    authApi.login.and.returnValue(of(response) as any);

    service.login('user@example.com', 'Secret123!').subscribe({
      next: () => {
        expect(localStorage.getItem('app_token')).toBe(token);
        expect(localStorage.getItem('app_refresh_token')).toBe('refresh-1');
        expect(localStorage.getItem('app_roles')).toBe(JSON.stringify(['admin']));
        done();
      },
      error: done.fail,
    });
  });

  it('identifies expired tokens', () => {
    const fresh = createJwt({ exp: futureExp });
    const expired = createJwt({ exp: pastExp });

    localStorage.setItem('app_token', fresh);
    expect(service.isAccessTokenExpired()).toBeFalse();

    localStorage.setItem('app_token', expired);
    expect(service.isAccessTokenExpired()).toBeTrue();
  });

  it('refreshTokens updates stored tokens when successful', (done) => {
    localStorage.setItem('app_refresh_token', 'refresh-1');
    const newToken = createJwt({ exp: futureExp, roles: ['ROLE_TEACHER'] });
    authApi.refresh.and.returnValue(
      of({ accessToken: newToken, refreshToken: 'refresh-2' }) as any,
    );

    service.refreshTokens().subscribe({
      next: () => {
        expect(localStorage.getItem('app_token')).toBe(newToken);
        expect(localStorage.getItem('app_refresh_token')).toBe('refresh-2');
        expect(localStorage.getItem('app_roles')).toBe(JSON.stringify(['teacher']));
        done();
      },
      error: done.fail,
    });
  });

  it('accepts single role claim named "role"', (done) => {
    const token = createJwt({ exp: futureExp, role: 'ADMIN' });
    const response: TokenResponse = { accessToken: token, refreshToken: 'refresh-1' };
    authApi.login.and.returnValue(of(response) as any);

    service.login('user@example.com', 'Secret123!').subscribe({
      next: () => {
        expect(localStorage.getItem('app_roles')).toBe(JSON.stringify(['admin']));
        done();
      },
      error: done.fail,
    });
  });

  it('refreshTokens logs out when refresh fails', (done) => {
    const logSpy = spyOn(console, 'log').and.stub();
    const errSpy = spyOn(console, 'error').and.stub();
    localStorage.setItem('app_refresh_token', 'refresh-1');
    authApi.refresh.and.returnValue(throwError(() => new Error('invalid refresh')) as any);

    service.refreshTokens().subscribe({
      next: () => done.fail('expected error'),
      error: () => {
        expect(localStorage.getItem('app_token')).toBeNull();
        expect(router.navigate).toHaveBeenCalledWith(['/login']);
        done();
      },
    });
    logSpy.calls.reset();
    errSpy.calls.reset();
  });
});
