// Von Lukas bearbeitet
import { Injectable, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AdminService } from './admin.tokens';
import { AdminUser, Role } from './models';
import { API_BASE_URL } from '../../core/api.tokens';

@Injectable({ providedIn: 'root' })
export class AdminApiService implements AdminService {
  constructor(private http: HttpClient, @Inject(API_BASE_URL) private base: string) {}

  // Holt alle Nutzer (vom echten API-Backend)
  getUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.base}/users`);
  }

  // Neuen Nutzer anlegen
  addUser(name: string, email: string, roles: Role[]): Observable<void> {
    return this.http.post(`${this.base}/users`, { name, email, roles }).pipe(map(() => void 0));
  }

  // Nutzer löschen
  removeUser(id: number): Observable<void> {
    return this.http.delete(`${this.base}/users/${id}`).pipe(map(() => void 0));
  }

  // Rollen eines Nutzers anpassen
  setRoles(id: number, roles: Role[]): Observable<void> {
    return this.http.patch(`${this.base}/users/${id}/roles`, { roles }).pipe(map(() => void 0));
  }

  // Passwort zurücksetzen (nur Demo-Endpoint)
  resetPassword(id: number): Observable<void> {
    return this.http.post(`${this.base}/users/${id}/reset-password`, {}).pipe(map(() => void 0));
  }
}



