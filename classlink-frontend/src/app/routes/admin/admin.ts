import {ChangeDetectionStrategy, Component, ViewEncapsulation, OnInit, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AdminUser, Role} from '../../features/admin/models';
import {AdminSidebarComponent} from '../../features/admin/components/sidebar/sidebar';
import {AdminHeaderBarComponent} from '../../features/admin/components/header-bar/header-bar';
import {AdminKpiCardsComponent} from '../../features/admin/components/kpi-cards/kpi-cards';
import {AdminUserTableComponent} from '../../features/admin/components/user-table/user-table';
import {ADMIN_SERVICE, AdminService} from '../../features/admin/admin.service.contract';
import {AdminApiService} from '../../features/admin/admin.api.service';
import {Toast} from 'primeng/toast';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {ConfirmationService, MessageService} from 'primeng/api';
import {
    CreateInviteRequestDto,
    CreateInviteRequestDtoRoleEnum,
    InviteCreatedResponseDto,
    PasswordResetCreateRequestDto,
    PasswordResetCreateResponseDto
} from '../../api';
import {catchError, finalize, of} from 'rxjs';

@Component({
    standalone: true,
    selector: 'app-admin',
    imports: [
        CommonModule,
        FormsModule,
        AdminSidebarComponent,
        AdminHeaderBarComponent,
        AdminKpiCardsComponent,
        AdminUserTableComponent,
        Toast,
        ConfirmDialog,
    ],
    templateUrl: './admin.html',
    styleUrl: './admin.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {provide: ADMIN_SERVICE, useExisting: AdminApiService},
        MessageService,
        ConfirmationService,
    ],
    encapsulation: ViewEncapsulation.None,
})
export class AdminComponent implements OnInit {
    private readonly admin = inject<AdminService>(ADMIN_SERVICE);
    private readonly messages = inject(MessageService);
    private readonly confirm = inject(ConfirmationService);

    users = signal<AdminUser[]>([]);
    search = signal('');
    busy = signal(false);
    apiError = signal<string | null>(null);
    lastInviteUrl = signal<string | null>(null);
    lastResetUrl = signal<string | null>(null);
    showInviteDialog = signal(false);
    showResetDialog = signal(false);

    // Derived
    filteredUsers = computed(() => {
        const q = this.search().trim().toLowerCase();
        const users = this.users();
        if (!q) return users;

        return users.filter((u) => {
            const inName = u.name.toLowerCase().includes(q);
            const inEmail = u.email.toLowerCase().includes(q);
            const inRole = u.roles.some((r) => r.toLowerCase().includes(q));
            return inName || inEmail || inRole;
        });
    });

    kpis = computed(() => {
        const users = this.users();
        const total = users.length;
        const active = users.filter((u) => u.status === 'active').length;
        const admins = users.filter((u) => u.roles.includes('admin')).length;
        return {total, active, admins};
    });

    constructor() {
        console.log('AdminComponent AdminService implementation:', this.admin.constructor.name);
    }

    ngOnInit(): void {
        this.loadUsers();
    }

    onSearchChange = (v: string) => {
        this.search.set(v);
    };

    onCreateUser = (payload: CreateInviteRequestDto | { email?: string; roles?: Role[] }) => {
        const request = this.normalizeInvitePayload(payload);
        this.busy.set(true);
        this.admin
            .inviteUser(request)
            .pipe(finalize(() => this.busy.set(false)))
            .subscribe({
                next: (res: InviteCreatedResponseDto) => {
                    this.lastInviteUrl.set((res as any).inviteUrl ?? res.qrCodeUrl ?? null);
                    this.showInviteDialog.set(true);
                    this.messages.add({
                        severity: 'success',
                        summary: 'Einladung erstellt',
                        detail: request.email,
                    });
                },
                error: () =>
                    this.messages.add({
                        severity: 'error',
                        summary: 'Fehlgeschlagen',
                        detail: 'Einladung konnte nicht erstellt werden',
                    }),
            });
    };

    onDeleteUser = (id: string) => {
        const user = this.users().find((u) => u.id === id);
        this.confirm.confirm({
            message: `Benutzer ${user?.email || '#' + id} wirklich löschen?`,
            header: 'Löschen bestätigen',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Löschen',
            rejectLabel: 'Abbrechen',
            acceptButtonStyleClass: 'btn-danger',
            rejectButtonStyleClass: 'btn',
            accept: () => {
                this.busy.set(true);
                this.admin
                    .removeUser(id)
                    .pipe(finalize(() => this.busy.set(false)))
                    .subscribe({
                        next: () => {
                            this.users.update((list: AdminUser[]) => list.filter((u: AdminUser) => u.id !== id));
                            this.messages.add({severity: 'success', summary: 'Nutzer gelöscht'});
                        },
                        error: () =>
                            this.messages.add({
                                severity: 'error',
                                summary: 'Fehlgeschlagen',
                                detail: 'Nutzer konnte nicht gelöscht werden',
                            }),
                    });
            },
        });
    };

    onResetPassword = (id: string) => {
        const user = this.users().find((u) => u.id === id);
        this.confirm.confirm({
            message: `Passwort für ${user?.email || '#' + id} wirklich zurücksetzen?`,
            header: 'Zurücksetzen bestätigen',
            icon: 'pi pi-key',
            acceptLabel: 'Zurücksetzen',
            rejectLabel: 'Abbrechen',
            acceptButtonStyleClass: 'btn-accent',
            rejectButtonStyleClass: 'btn',
            accept: () => {
                this.busy.set(true);
                const request: PasswordResetCreateRequestDto = {userId: id};
                this.admin
                    .resetPassword(request)
                    .pipe(finalize(() => this.busy.set(false)))
                    .subscribe({
                        next: (res: PasswordResetCreateResponseDto) => {
                            this.lastResetUrl.set((res as any).resetUrl ?? res.token ?? null);
                            this.showResetDialog.set(true);
                            this.messages.add({severity: 'success', summary: 'Passwort zurückgesetzt'});
                        },
                        error: () =>
                            this.messages.add({
                                severity: 'error',
                                summary: 'Fehlgeschlagen',
                                detail: 'Passwort konnte nicht zurückgesetzt werden',
                            }),
                    });
            },
        });
    };

    private loadUsers(): void {
        this.busy.set(true);
        this.apiError.set(null);

        this.admin
            .loadUsers()
            .pipe(
                catchError((error) => {
                    console.error('Failed to load users via AdminService', error);
                    this.apiError.set('Konnte Benutzerliste nicht laden');
                    return of([] as AdminUser[]);
                }),
                finalize(() => this.busy.set(false)),
            )
            .subscribe((users) => {
                this.users.set(users);
                console.log('[AdminComponent] users signal initialized/updated', users);
            });
    }

    private normalizeInvitePayload(payload: CreateInviteRequestDto | {
        email?: string;
        roles?: Role[]
    }): CreateInviteRequestDto {
        if ('role' in payload) {
            return payload;
        }
        const firstRole = (payload.roles && payload.roles[0]) || 'student';
        const role = this.mapRoleToDto(firstRole);
        return {
            email: payload.email,
            role,
        };
    }

    private mapRoleToDto(role: Role): CreateInviteRequestDtoRoleEnum {
        switch (role) {
            case 'admin':
                return CreateInviteRequestDtoRoleEnum.Admin;
            case 'teacher':
                return CreateInviteRequestDtoRoleEnum.Teacher;
            case 'student':
            default:
                return CreateInviteRequestDtoRoleEnum.Student;
        }
    }
}
