// Von Lukas bearbeitet
import {InjectionToken} from '@angular/core';
import {AdminUser, Role} from './models';
import {Observable} from 'rxjs';
import {
    CreateInviteRequestDto,
    InviteCreatedResponseDto,
    PasswordResetCreateRequestDto,
    PasswordResetCreateResponseDto
} from "../../api";

// Beschreibt, was der Admin-Service können soll (API oder Mock)
export interface AdminService {
    loadUsers(): Observable<AdminUser[]>;
    inviteUser(request: CreateInviteRequestDto): Observable<InviteCreatedResponseDto>;
    removeUser(id: string): Observable<void>;
    resetPassword(request: PasswordResetCreateRequestDto): Observable<PasswordResetCreateResponseDto>;
}

export const ADMIN_SERVICE = new InjectionToken<AdminService>('ADMIN_SERVICE');
