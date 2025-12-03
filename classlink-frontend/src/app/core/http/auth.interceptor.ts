import {HttpInterceptorFn, HttpRequest} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../../../services/auth.service';
import {switchMap, throwError} from 'rxjs';

function isRefreshRequest(req: HttpRequest<unknown>): boolean {
    return req.url.includes('/auth/refresh');
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);

    if (isRefreshRequest(req)) {
        return next(req);
    }

    const accessToken = auth.getAccessToken();
    if (!accessToken) {
        return next(req);
    }

    if (!auth.isAccessTokenExpired()) {
        const authorized = attachToken(req, accessToken);
        return next(authorized);
    }

    return auth.refreshTokens().pipe(
        switchMap(() => {
            const newToken = auth.getAccessToken();
            if (!newToken) {
                return throwError(() => new Error('Refresh succeeded but no access token present'));
            }
            const retryReq = attachToken(req, newToken);
            return next(retryReq);
        })
    );
};

function attachToken(req: HttpRequest<unknown>, token: string | null) {
  if (token) {
      return req.clone({setHeaders: {Authorization: `Bearer ${token}`}});
  }
    return req;
}
