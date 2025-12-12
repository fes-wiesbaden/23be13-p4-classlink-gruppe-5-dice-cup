// admin.api.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, map, tap, switchMap, from, of} from 'rxjs';
import {AdminService} from './admin.service.contract';
import { AdminUser, Role } from './models';
import { API_BASE_URL } from '../../core/api.tokens';
import {
    CreateInviteRequestDto,
    InvitationControllerService,
    InviteCreatedResponseDto,
    PasswordResetControllerService,
    PasswordResetCreateRequestDto,
    PasswordResetCreateResponseDto,
    UserControllerService,
    UserDto,
} from '../../api';

@Injectable({ providedIn: 'root' })
export class AdminApiService implements AdminService {
    private http = inject(HttpClient);
    private base = inject(API_BASE_URL);

    constructor(
        private readonly userApi: UserControllerService,
        private inviteApi: InvitationControllerService,
        private passwordApi: PasswordResetControllerService,
    ) {
    }

    // Holt alle Nutzer (vom echten API-Backend)
    loadUsers(): Observable<AdminUser[]> {
        return this.userApi.getUsers().pipe(
            tap((raw) =>
                console.log('[AdminApiService] raw getUsers response', raw),
            ),
            switchMap((raw: any) => {
                // Fall 1: Generator liefert Blob (wie aktuell)
                if (raw instanceof Blob) {
                    return from(raw.text()).pipe(
                        map((text) => JSON.parse(text) as UserDto[]),
                    );
                }

                // Fall 2: Irgendwann später liefert der Client direkt JSON
                if (Array.isArray(raw)) {
                    return of(raw as UserDto[]);
                }

                console.warn(
                    '[AdminApiService] unexpected getUsers response shape, returning []',
                    raw,
                );
                return of([] as UserDto[]);
            }),
            map((dtos: UserDto[]) => dtos.map((dto) => this.mapUserDto(dto))),
            tap((users) =>
                console.log('[AdminApiService] mapped users', users),
            ),
        );
    }

    inviteUser(request: CreateInviteRequestDto): Observable<InviteCreatedResponseDto> {
        return this.inviteApi.create(request);
    }

    removeUser(userId: string): Observable<void> {
        return this.userApi.deleteUser(userId).pipe(map(() => void 0));
    }

    resetPassword(
        request: PasswordResetCreateRequestDto,
    ): Observable<PasswordResetCreateResponseDto> {
        return this.passwordApi.create1(request);
    }

    private mapUserDto(dto: UserDto): AdminUser {
        const first = dto.userInfo?.firstName?.trim() ?? '';
        const last = dto.userInfo?.lastName?.trim() ?? '';
        const name =
            `${first} ${last}`.trim() || dto.username || 'Unbekannter Nutzer';
        const email = dto.userInfo?.email || dto.username || '';

        return {
            id: dto.id!,
            name,
            email,
            roles: this.mapRoles(dto),
            status: dto.enabled ? 'active' : 'disabled',
            createdAt: (dto as any)?.createdAt,
        };
    }

    private mapRoles(dto: UserDto): Role[] {
        if (!dto.role) return [];

        switch (dto.role) {
            case 'ADMIN':
                return ['admin'];
            case 'TEACHER':
                return ['teacher'];
            case 'STUDENT':
                return ['student'];
            default:
                return [];
        }
    }
}