// Von Lukas bearbeitet
import {Injectable} from '@angular/core';
import {AdminUser, Role} from './models';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {AdminService} from './admin.service.contract';
import {
    CreateInviteRequestDto,
    InviteCreatedResponseDto,
    PasswordResetCreateRequestDto,
    PasswordResetCreateResponseDto
} from '../../api';

@Injectable({ providedIn: 'root' })
export class AdminMockService implements AdminService {
  private state: AdminUser[] = [
    {
        id: '1',
      name: 'Anna Schmidt',
      email: 'anna.schmidt@example.com',
      roles: ['student'],
      status: 'active',
      createdAt: new Date(2024, 0, 12).toISOString(),
    },
    {
        id: '2',
      name: 'Max Müller',
      email: 'max.mueller@example.com',
      roles: ['teacher'],
      status: 'active',
      createdAt: new Date(2024, 2, 5).toISOString(),
    },
    {
        id: '3',
      name: 'Lena Wagner',
      email: 'lena.wagner@example.com',
      roles: ['admin'],
      status: 'active',
      createdAt: new Date(2024, 5, 20).toISOString(),
    },
    {
        id: '4',
      name: 'Tim Becker',
      email: 'tim.becker@example.com',
      roles: ['student'],
      status: 'disabled',
      createdAt: new Date(2023, 10, 2).toISOString(),
    },
  ];

  private nextId = 5;
  private users$ = new BehaviorSubject<AdminUser[]>([...this.state]);

  // Gibt den aktuellen Stand als Observable raus
  getUsers(): Observable<AdminUser[]> {
    return this.users$.asObservable();
  }

  // Fügt einen Nutzer lokal hinzu (Mock)
    inviteUser(request: CreateInviteRequestDto): Observable<InviteCreatedResponseDto> {
        const name = request.email ?? `User ${this.nextId}`;
        const email = request.email ?? `user${this.nextId}@example.com`;
        const roles: Role[] = [this.mapRole(request.role)];

    this.state = [
      ...this.state,
      {
          id: String(this.nextId++),
        name,
        email,
        roles,
        status: 'active',
        createdAt: new Date().toISOString(),
      },
    ];
    this.users$.next([...this.state]);
        return of({inviteId: 'mock', token: 'mock-token', expiresAt: new Date(Date.now() + 3600_000).toISOString()});
  }

  // Entfernt einen Nutzer (Mock)
    removeUser(id: string): Observable<void> {
    this.state = this.state.filter((u) => u.id !== id);
    this.users$.next([...this.state]);
    return of(void 0);
  }

  // Nur eine Demo-Ausgabe hier
    resetPassword(request: PasswordResetCreateRequestDto): Observable<PasswordResetCreateResponseDto> {
        console.info('Password reset for user', request.userId);
        return of({
            tokenId: 'mock-reset',
            token: 'mock-reset-token',
            expiresAt: new Date(Date.now() + 3600_000).toISOString(),
            userId: request.userId,
        });
    }

    // Aliases to satisfy interface naming
    loadUsers(): Observable<AdminUser[]> {
        return this.getUsers();
    }

    private mapRole(role: CreateInviteRequestDto['role']): Role {
        switch (role) {
            case 'ADMIN':
                return 'admin';
            case 'TEACHER':
                return 'teacher';
            case 'STUDENT':
            default:
                return 'student';
        }
  }
}
