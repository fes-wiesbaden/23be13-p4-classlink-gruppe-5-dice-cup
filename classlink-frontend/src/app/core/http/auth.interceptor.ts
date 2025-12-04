import {
    HttpContextToken,
    HttpErrorResponse,
    HttpHandlerFn,
    HttpInterceptorFn,
    HttpRequest,
} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../../../services/auth.service';
import {Observable, catchError, map, of, switchMap, throwError} from 'rxjs';

const RETRY_CONTEXT = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);

    if (shouldBypassAuth(req)) {
        return next(req);
    }

    return ensureFreshToken(auth).pipe(
        switchMap((didRefresh) => {
            const preparedRequest = didRefresh ? markAsRetried(req) : req;
            const authorized = attachToken(preparedRequest, auth.getAccessToken());
            return next(authorized).pipe(catchError((error) => handleAuthError(error, authorized, next, auth)));
        })
    );
};

function ensureFreshToken(auth: AuthService): Observable<boolean> {
    const currentToken = auth.getAccessToken();
    if (!currentToken) {
        return of(false);
    }
    if (auth.isAccessTokenExpired()) {
        return auth.refreshTokens().pipe(map(() => true));
    }
    return of(false);
}

function handleAuthError(
    error: unknown,
    request: HttpRequest<unknown>,
    next: HttpHandlerFn,
    auth: AuthService
) {
    if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
    }

    if (error.status === 401 && shouldAttemptRetry(request, auth)) {
        return auth.refreshTokens().pipe(
            switchMap(() => {
                const freshToken = auth.getAccessToken();
                if (!freshToken) {
                    return throwError(() => new Error('Refresh succeeded without an access token'));
                }
                const retriedRequest = attachToken(markAsRetried(request), freshToken);
                return next(retriedRequest);
            }),
            catchError((refreshError) => throwError(() => refreshError))
        );
    }

    return throwError(() => error);
}

function shouldBypassAuth(req: HttpRequest<unknown>): boolean {
    return req.url.includes('/auth/login') || req.url.includes('/auth/refresh');
}

function shouldAttemptRetry(req: HttpRequest<unknown>, auth: AuthService): boolean {
    return !req.context.get(RETRY_CONTEXT) && !!auth.getRefreshToken();
}

function markAsRetried(req: HttpRequest<unknown>): HttpRequest<unknown> {
    if (req.context.get(RETRY_CONTEXT)) {
        return req;
    }
    return req.clone({context: req.context.set(RETRY_CONTEXT, true)});
}

function attachToken(req: HttpRequest<unknown>, token: string | null) {
    if (token) {
        return req.clone({setHeaders: {Authorization: `Bearer ${token}`}});
    }
    return req;
}
