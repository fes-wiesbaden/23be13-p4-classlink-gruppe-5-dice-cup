// Von Lukas bearbeitet
import { Injectable } from '@angular/core';
import { AdminUser, Role } from './models';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { AdminService } from './admin.tokens';

@Injectable({ providedIn: 'root' })
export class AdminMockService implements AdminService {
  private state: AdminUser[] = [
    { id: 1, name: 'Anna Schmidt',  email: 'anna.schmidt@example.com',  roles: ['student'], status: 'active',   createdAt: new Date(2024, 0, 12).toISOString() },
    { id: 2, name: 'Max MÃ¼ller',    email: 'max.mueller@example.com',   roles: ['teacher'], status: 'active',   createdAt: new Date(2024, 2, 5).toISOString() },
    { id: 3, name: 'Lena Wagner',   email: 'lena.wagner@example.com',   roles: ['admin'],   status: 'active',   createdAt: new Date(2024, 5, 20).toISOString() },
    { id: 4, name: 'Tim Becker',    email: 'tim.becker@example.com',    roles: ['student'], status: 'disabled', createdAt: new Date(2023, 10, 2).toISOString() },
  ];

  private nextId = 5;
  private users$ = new BehaviorSubject<AdminUser[]>([...this.state]);

  // Gibt den aktuellen Stand als Observable raus
  getUsers(): Observable<AdminUser[]> { return this.users$.asObservable(); }

  // Fügt einen Nutzer lokal hinzu (Mock)
  addUser(name: string, email: string, roles: Role[] = ['student']): Observable<void> {
    this.state = [
      ...this.state,
      { id: this.nextId++, name, email, roles, status: 'active', createdAt: new Date().toISOString() },
    ];
    this.users$.next([...this.state]);
    return of(void 0);
  }

  // Entfernt einen Nutzer (Mock)
  removeUser(id: number): Observable<void> {
    this.state = this.state.filter(u => u.id !== id);
    this.users$.next([...this.state]);
    return of(void 0);
  }

  // Speichert neue Rollen (Mock)
  setRoles(id: number, roles: Role[]): Observable<void> {
    this.state = this.state.map(u => u.id === id ? { ...u, roles: [...roles] } : u);
    this.users$.next([...this.state]);
    return of(void 0);
  }

  // Nur eine Demo-Ausgabe hier
  resetPassword(id: number): Observable<void> {
    console.info('Password reset for user', id);
    return of(void 0);
  }
}


