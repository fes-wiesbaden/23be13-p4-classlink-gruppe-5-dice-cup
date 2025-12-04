import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {AuthControllerService, LoginRequest, RefreshRequest, TokenResponse} from '../app/api';
import {Observable, catchError, finalize, from, map, of, shareReplay, switchMap, tap, throwError} from 'rxjs';

interface DecodedJwt {
    sub?: string;
    roles?: string[] | string;
    role?: string[] | string;
    authorities?: string[] | string;
    scope?: string;

    [key: string]: unknown;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY = 'app_token';
    private readonly REFRESH_KEY = 'app_refresh_token';
    private readonly ROLES_KEY = 'app_roles';
    private readonly USERNAME_KEY = 'app_user';
    private refreshInProgress$: Observable<void> | null = null;

    private readonly authApi = inject(AuthControllerService);
    private readonly router = inject(Router);

    login(email: string, password: string): Observable<void> {
        const body: LoginRequest = {email, password};
        this.log('Attempting login', email);
        return this.authApi.login(body).pipe(
            switchMap((response) => this.normalizeTokenResponse(response)),
            tap((response) => {
                this.log('Login successful');
                this.persistAuth(response, email);
            }),
            map(() => void 0),
            catchError((error) => {
                this.log('Login failed', error);
                return throwError(() => error);
            })
        );
  }

    private performRefresh(): Observable<void> {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            throw new Error('Missing refresh token');
        }
        this.log('Refreshing access token');
        const body: RefreshRequest = {refreshToken};
        return this.authApi.refresh(body).pipe(
            switchMap((response) => this.normalizeTokenResponse(response)),
            tap((response) => {
                this.log('Refresh successful');
                this.persistAuth(response);
            }),
            map(() => void 0)
        );
  }

    refreshTokens(): Observable<void> {
        if (!this.getRefreshToken()) {
            return throwError(() => new Error('No refresh token available'));
        }

        if (!this.refreshInProgress$) {
            this.refreshInProgress$ = this.performRefresh().pipe(
                finalize(() => {
                    this.refreshInProgress$ = null;
                }),
                catchError((error) => {
                    this.log('Refresh failed; logging out', error);
                    this.logout();
                    return throwError(() => error);
                }),
                shareReplay(1)
            );
    }

        return this.refreshInProgress$;
  }

  logout(): void {
      this.log('Clearing auth state');
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_KEY);
      localStorage.removeItem(this.ROLES_KEY);
      localStorage.removeItem(this.USERNAME_KEY);
      this.router.navigate(['/login']).catch(console.error);
  }

    isLoggedIn(): boolean {
        return !!this.getAccessToken();
    }

    hasRole(required: string | string[]): boolean {
        const roles = this.getRoles();
        const list = Array.isArray(required) ? required : [required];
        return list.some((role) => roles.includes(role));
    }

    getRoles(): string[] {
        try {
            const stored = localStorage.getItem(this.ROLES_KEY);
            return stored ? (JSON.parse(stored) as string[]) : [];
        } catch {
            return [];
        }
    }

    getAccessToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    getRefreshToken(): string | null {
        return localStorage.getItem(this.REFRESH_KEY);
    }

    isAccessTokenExpired(): boolean {
        const token = this.getAccessToken();
        if (!token) {
            return true;
        }
        try {
            const [, payload] = token.split('.');
            if (!payload) {
                return true;
            }
            const decoded = JSON.parse(this.base64UrlDecode(payload)) as { exp?: number };
            if (!decoded.exp) {
                return false;
            }
            const expiryMs = decoded.exp * 1000;
            return Date.now() >= expiryMs;
        } catch (error) {
            this.log('Failed to evaluate token expiry', error);
            return true;
        }
  }

  getUsername(): string | null {
      return localStorage.getItem(this.USERNAME_KEY);
  }

  setRoles(roles: string[]): void {
      this.log('Updating roles', roles);
      localStorage.setItem(this.ROLES_KEY, JSON.stringify(roles));
  }

    private normalizeTokenResponse(res: TokenResponse | Blob): Observable<TokenResponse> {
        if (res instanceof Blob) {
            return from(res.text()).pipe(map((text) => JSON.parse(text) as TokenResponse));
        }
        return of(res);
    }

    private persistAuth(res: TokenResponse, usernameHint?: string): void {
        const payload = res as TokenResponse & {
            token?: string;
            access_token?: string;
            refresh_token?: string;
        };
        const accessToken = payload.accessToken ?? payload.access_token ?? payload.token;
        const refreshToken = payload.refreshToken ?? payload.refresh_token ?? null;
        if (!accessToken) {
            throw new Error('Access token missing in response');
        }
        localStorage.setItem(this.TOKEN_KEY, accessToken);
        if (refreshToken) {
            localStorage.setItem(this.REFRESH_KEY, refreshToken);
        }
        if (usernameHint) {
            localStorage.setItem(this.USERNAME_KEY, usernameHint);
        }

        const roles = this.extractRoles(accessToken);
        localStorage.setItem(this.ROLES_KEY, JSON.stringify(roles));
        this.log('Auth data persisted');
    }

    private extractRoles(token: string): string[] {
        try {
            const [, payload] = token.split('.');
            if (!payload) {
                return [];
            }
            const json = this.base64UrlDecode(payload);
            const decoded = JSON.parse(json) as DecodedJwt;
            const rawRoles =
                decoded.roles ??
                decoded.role ??
                decoded.authorities ??
                (typeof decoded.scope === 'string' ? decoded.scope.split(' ') : []);
            return this.normalizeRoles(rawRoles);
        } catch (err) {
            this.log('Failed to decode roles', err);
            return [];
        }
    }

    private normalizeRoles(value: unknown): string[] {
        if (!value) {
            return [];
        }
        if (Array.isArray(value)) {
            return value.map((role) => this.stripRolePrefix(String(role)));
        }
        if (typeof value === 'string') {
            return value
                .split(/[, ]+/)
                .filter(Boolean)
                .map((role) => this.stripRolePrefix(role));
        }
        return [];
    }

    private stripRolePrefix(role: string): string {
        return role.replace(/^ROLE_/i, '').toLowerCase();
    }

    private base64UrlDecode(value: string): string {
        const padded = value.replace(/-/g, '+').replace(/_/g, '/');
        const pad = padded.length % 4;
        const normalized = pad ? padded + '='.repeat(4 - pad) : padded;
        return atob(normalized);
  }

    private log(message: string, payload?: unknown): void {
        if (payload !== undefined) {
            console.log(`[AuthService] ${message}`, payload);
        } else {
            console.log(`[AuthService] ${message}`);
        }
    }
}
