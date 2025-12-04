import {HTTP_INTERCEPTORS, HttpClient, HttpErrorResponse, HttpHandler, HttpRequest} from '@angular/common/http';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {AuthService} from '../../../services/auth.service';
import {authInterceptor} from './auth.interceptor';

describe('authInterceptor', () => {
    let http: HttpClient;
    let httpMock: HttpTestingController;
    let authService: jasmine.SpyObj<AuthService>;

    beforeEach(() => {
        authService = jasmine.createSpyObj('AuthService', ['getAccessToken', 'isAccessTokenExpired', 'refreshTokens']);

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                {provide: AuthService, useValue: authService},
                {
                    provide: HTTP_INTERCEPTORS,
                    useFactory: () => ({
                        intercept: (req: HttpRequest<unknown>, next: HttpHandler) =>
                            authInterceptor(req, (forwardReq) => next.handle(forwardReq)),
                    }),
                    multi: true,
                },
            ],
        });

        http = TestBed.inject(HttpClient);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('leaves requests untouched when no token is present', () => {
        authService.getAccessToken.and.returnValue(null);

        http.get('/data').subscribe();

        const req = httpMock.expectOne('/data');
        expect(req.request.headers.has('Authorization')).toBeFalse();
        req.flush({});
    });

    it('attaches bearer token when token is valid', () => {
        authService.getAccessToken.and.returnValue('token-123');
        authService.isAccessTokenExpired.and.returnValue(false);

        http.get('/secure').subscribe();

        const req = httpMock.expectOne('/secure');
        expect(req.request.headers.get('Authorization')).toBe('Bearer token-123');
        req.flush({});
    });

    it('refreshes token before sending request when expired', () => {
        authService.getAccessToken.and.returnValues('expired-token', 'fresh-token');
        authService.isAccessTokenExpired.and.returnValue(true);
        authService.refreshTokens.and.returnValue(of(void 0));

        http.get('/secure').subscribe();

        const req = httpMock.expectOne('/secure');
        expect(req.request.headers.get('Authorization')).toBe('Bearer fresh-token');
        req.flush({});
        expect(authService.refreshTokens).toHaveBeenCalled();
    });

    it('propagates errors when refresh fails', (done) => {
        authService.getAccessToken.and.returnValue('expired-token');
        authService.isAccessTokenExpired.and.returnValue(true);
        authService.refreshTokens.and.returnValue(throwError(() => new Error('refresh failed')));

        http.get('/secure').subscribe({
            next: () => done.fail('expected error'),
            error: (error: HttpErrorResponse | Error) => {
                expect(error).toBeTruthy();
                done();
            },
        });

        httpMock.expectNone('/secure');
    });
});
