// Von Lukas bearbeitet
import { InjectionToken } from '@angular/core';
import { AdminUser, Role } from './models';
import { Observable } from 'rxjs';

// Beschreibt, was der Admin-Service können soll (API oder Mock)
export interface AdminService {
  getUsers(): Observable<AdminUser[]>;
  addUser(name: string, email: string, roles: Role[]): Observable<void>;
  removeUser(id: number): Observable<void>;
  setRoles(id: number, roles: Role[]): Observable<void>;
  resetPassword(id: number): Observable<void>;
}

export const ADMIN_SERVICE = new InjectionToken<AdminService>('ADMIN_SERVICE');
