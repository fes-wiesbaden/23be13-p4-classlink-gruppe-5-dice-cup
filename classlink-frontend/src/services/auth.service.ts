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

export interface AuthSnapshot {
    accessToken: string | null;
    refreshToken: string | null;
    email?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY = 'app_token';
    private readonly REFRESH_KEY = 'app_refresh_token';
    private readonly ROLES_KEY = 'app_roles';
    private readonly USERNAME_KEY = 'app_user';
    private refreshInProgress$: Observable<void> | null = null;
    private impersonatedAccessToken: string | null = null;
    private impersonatedRefreshToken: string | null = null;

    private readonly authApi = inject(AuthControllerService);
    private readonly router = inject(Router);

    login(email: string, password: string): Observable<string> {
        const body: LoginRequest = {email, password};
        this.log('Attempting login', email);
        return this.authApi.login(body).pipe(
            switchMap((response) => this.normalizeTokenResponse(response)),
            tap((response) => {
                this.log('Login successful, raw response', response);
                this.persistAuth(response, email);
                this.log('Decoded roles after login', this.getRoles());
            }),
            map(() => this.getHomeRoute()),
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
      this.log('Clearing auth state (including impersonation)');
      this.stopImpersonation();
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_KEY);
      localStorage.removeItem(this.ROLES_KEY);
      localStorage.removeItem(this.USERNAME_KEY);
      this.log(
          `After logout, effective token: ${this.getEffectiveAccessToken()} roles: ${JSON.stringify(this.getRoles())}`
      );
      this.router.navigate(['/login']).catch(console.error);
  }

    isLoggedIn(): boolean {
        return !!this.getEffectiveAccessToken();
    }

    hasRole(required: string | string[]): boolean {
        const roles = this.getRoles();
        const list = Array.isArray(required) ? required : [required];
        return list.some((role) => roles.includes(role));
    }

    getRoles(): string[] {
        const token = this.getEffectiveAccessToken();
        if (token) {
            const decoded = this.extractRoles(token);
            if (decoded.length) {
                return decoded;
            }
        }
        // fallback to stored roles if token missing or failed decode
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

    // Effective token prefers impersonation overlay if present
    getEffectiveAccessToken(): string | null {
        return this.impersonatedAccessToken ?? this.getAccessToken();
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

  setRoles(_roles: string[]): void {
      // UI-driven role switching is disabled; roles must come from JWT
      this.log('Ignoring setRoles call; roles are derived from JWT only');
  }

    // DEV-ONLY: snapshot/restore current auth state (used by DevDock for impersonation)
    createSnapshot(): AuthSnapshot | null {
        const accessToken = this.getAccessToken();
        if (!accessToken) {
            return null;
        }
        return {
            accessToken,
            refreshToken: this.getRefreshToken(),
            email: this.getUsername(),
        };
    }

    restoreSnapshot(snapshot: AuthSnapshot | null): void {
        if (!snapshot) {
            return;
        }
        if (snapshot.accessToken) {
            localStorage.setItem(this.TOKEN_KEY, snapshot.accessToken);
        } else {
            localStorage.removeItem(this.TOKEN_KEY);
        }
        if (snapshot.refreshToken) {
            localStorage.setItem(this.REFRESH_KEY, snapshot.refreshToken);
        } else {
            localStorage.removeItem(this.REFRESH_KEY);
        }
        if (snapshot.email) {
            localStorage.setItem(this.USERNAME_KEY, snapshot.email);
        } else {
            localStorage.removeItem(this.USERNAME_KEY);
        }
        // roles will be re-derived from the token on next getRoles() call
    }

    // DEV-ONLY: impersonation overlay (in-memory only)
    startImpersonation(accessToken: string, refreshToken?: string | null): void {
        this.impersonatedAccessToken = accessToken;
        this.impersonatedRefreshToken = refreshToken ?? null;
        this.log('Impersonation started');
    }

    stopImpersonation(): void {
        this.impersonatedAccessToken = null;
        this.impersonatedRefreshToken = null;
        this.log('Impersonation stopped');
    }

    isImpersonating(): boolean {
        return this.impersonatedAccessToken != null;
    }

    impersonationLogin(email: string, password: string): Observable<void> {
        const body: LoginRequest = { email, password };
        this.log('Impersonation login start', email);
        return this.authApi.login(body).pipe(
            switchMap((response) => this.normalizeTokenResponse(response)),
            tap((response) => {
                this.log('Impersonation login successful, raw response', response);
                const payload = response as TokenResponse & {
                    token?: string;
                    access_token?: string;
                    refresh_token?: string;
                };
                const accessToken = payload.accessToken ?? payload.access_token ?? payload.token;
                const refreshToken = payload.refreshToken ?? payload.refresh_token ?? null;
                if (!accessToken) {
                    throw new Error('Access token missing in impersonation response');
                }
                this.startImpersonation(accessToken, refreshToken);
                this.log('Impersonation roles', this.getRoles());
            }),
            map(() => void 0),
            catchError((error) => {
                this.log('Impersonation login failed', error);
                return throwError(() => error);
            })
        );
    }

    getHomeRoute(): string {
        const roles = this.getRoles();
        if (roles.includes('admin')) return '/admin';
        if (roles.includes('teacher')) return '/teacher';
        if (roles.includes('student')) return '/student';
        return '/login';
    }

    private normalizeTokenResponse(res: TokenResponse | Blob | string): Observable<TokenResponse> {
        if (res instanceof Blob) {
            return from(res.text()).pipe(map((text) => JSON.parse(text) as TokenResponse));
        }
        if (typeof res === 'string') {
            try {
                return of(JSON.parse(res) as TokenResponse);
            } catch (err) {
                this.log('Failed to parse token response string', err);
                return throwError(() => err);
            }
        }
        return of(res as TokenResponse);
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
