import { Component, ViewEncapsulation, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUser, Role } from '../../features/admin/models';
import { AdminSidebarComponent } from '../../features/admin/components/sidebar/sidebar';
import { AdminHeaderBarComponent } from '../../features/admin/components/header-bar/header-bar';
import { AdminKpiCardsComponent } from '../../features/admin/components/kpi-cards/kpi-cards';
import { AdminUserTableComponent } from '../../features/admin/components/user-table/user-table';
import { ADMIN_SERVICE, AdminService } from '../../features/admin/admin.tokens';
import { Inject } from '@angular/core';
import { AdminMockService } from '../../features/admin/mock.service';
import { Toast } from 'primeng/toast';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';

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
    // Swap to ApiService later without touching components
    AdminMockService,
    { provide: ADMIN_SERVICE, useExisting: AdminMockService },
    MessageService,
    ConfirmationService,
  ],
  encapsulation: ViewEncapsulation.None,
})
export class AdminComponent {
  constructor(
    @Inject(ADMIN_SERVICE) private readonly admin: AdminService,
    private readonly messages: MessageService,
    private readonly confirm: ConfirmationService,
  ) {
    this.admin.getUsers().subscribe(users => { this.users = users; this.updateFiltered(); });
  }

  // State
  users: AdminUser[] = [];
  search = '';
  busy = false;

  // Derived
  filteredUsers: AdminUser[] = [];
  private updateFiltered() {
    const q = this.search.trim().toLowerCase();
    this.filteredUsers = !q
      ? this.users
      : this.users.filter(u =>
          u.name.toLowerCase().includes(q) ||
          u.email.toLowerCase().includes(q) ||
          u.roles.some(r => r.toLowerCase().includes(q))
        );
  }

  get kpis() {
    const total = this.users.length;
    const active = this.users.filter(u => u.status === 'active').length;
    const admins = this.users.filter(u => u.roles.includes('admin')).length;
    return { total, active, admins };
  }

  // Handlers
  onSearchChange = (v: string) => { this.search = v; this.updateFiltered(); };

  onCreateUser = (payload: { name: string; email: string; roles: Role[] }) => {
    this.busy = true;
    this.admin.addUser(payload.name, payload.email, payload.roles).subscribe({
      next: () => this.messages.add({ severity: 'success', summary: 'Nutzer erstellt', detail: payload.email }),
      error: () => this.messages.add({ severity: 'error', summary: 'Fehlgeschlagen', detail: 'Nutzer konnte nicht erstellt werden' }),
      complete: () => this.busy = false,
    });
  };

  onDeleteUser = (id: number) => {
    const user = this.users.find(u => u.id === id);
    this.confirm.confirm({
      message: `Benutzer ${user?.email || '#'+id} wirklich löschen?`,
      header: 'Löschen bestätigen',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Löschen',
      rejectLabel: 'Abbrechen',
      acceptButtonStyleClass: 'btn-danger',
      rejectButtonStyleClass: 'btn',
      accept: () => {
        this.busy = true;
        this.admin.removeUser(id).subscribe({
          next: () => this.messages.add({ severity: 'success', summary: 'Nutzer gelöscht' }),
          error: () => this.messages.add({ severity: 'error', summary: 'Fehlgeschlagen', detail: 'Nutzer konnte nicht gelöscht werden' }),
          complete: () => this.busy = false,
        });
      },
    });
  };

  onResetPassword = (id: number) => {
    const user = this.users.find(u => u.id === id);
    this.confirm.confirm({
      message: `Passwort für ${user?.email || '#'+id} wirklich zurücksetzen?`,
      header: 'Zurücksetzen bestätigen',
      icon: 'pi pi-key',
      acceptLabel: 'Zurücksetzen',
      rejectLabel: 'Abbrechen',
      acceptButtonStyleClass: 'btn-accent',
      rejectButtonStyleClass: 'btn',
      accept: () => {
        this.busy = true;
        this.admin.resetPassword(id).subscribe({
          next: () => this.messages.add({ severity: 'success', summary: 'Passwort zurückgesetzt' }),
          error: () => this.messages.add({ severity: 'error', summary: 'Fehlgeschlagen', detail: 'Passwort konnte nicht zurückgesetzt werden' }),
          complete: () => this.busy = false,
        });
      },
    });
  };

  onUpdateRoles = (id: number, roles: Role[]) => {
    this.busy = true;
    this.admin.setRoles(id, roles).subscribe({
      next: () => this.messages.add({ severity: 'success', summary: 'Rollen aktualisiert' }),
      error: () => this.messages.add({ severity: 'error', summary: 'Fehlgeschlagen', detail: 'Rollen konnten nicht aktualisiert werden' }),
      complete: () => this.busy = false,
    });
  };
}
