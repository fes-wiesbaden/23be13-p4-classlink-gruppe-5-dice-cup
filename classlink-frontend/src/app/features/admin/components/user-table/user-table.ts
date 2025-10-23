// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUser, Role } from '../../models';
import { RoleLabelPipe } from '../../pipes/role-label.pipe';

@Component({
  standalone: true,
  selector: 'admin-user-table',
  imports: [CommonModule, FormsModule, RoleLabelPipe],
  templateUrl: './user-table.html',
  styleUrl: './user-table.scss',
})
export class AdminUserTableComponent {
  @Input() users: AdminUser[] = [];
  @Input() busy = false;

  @Output() createUser = new EventEmitter<{ name: string; email: string; roles: Role[] }>();
  @Output() deleteUser = new EventEmitter<number>();
  @Output() resetPassword = new EventEmitter<number>();
  @Output() updateRoles = new EventEmitter<{ id: number; roles: Role[] }>();

  // Inline create form (secondary, header has quick-create as well)
  name = '';
  email = '';
  newRoles: Role[] = ['student'];

  // Erstellt unten in der Tabelle einen neuen Nutzer
  add() {
    const name = this.name.trim();
    const email = this.email.trim();
    if (!name || !email) return;
    this.createUser.emit({ name, email, roles: [...this.newRoles] });
    this.name = '';
    this.email = '';
    this.newRoles = ['student'];
  }

  // Rolle im Erstellen-Formular umschalten
  toggleNewRole(role: Role) {
    this.newRoles = this.newRoles.includes(role)
      ? this.newRoles.filter(x => x !== role)
      : [...this.newRoles, role];
  }

  // Roles editing per row
  editingId: number | null = null;
  editRoles: Role[] = [];

  // Startet das Rollen-Bearbeiten für eine Zeile
  beginEdit(u: AdminUser) {
    this.editingId = u.id;
    this.editRoles = [...u.roles];
  }
  // Bricht das Bearbeiten wieder ab
  cancelEdit() {
    this.editingId = null;
    this.editRoles = [];
  }
  // Speichert die neuen Rollen und beendet Bearbeiten
  saveEdit(id: number) {
    this.updateRoles.emit({ id, roles: [...this.editRoles] });
    this.cancelEdit();
  }

  // Für Checkboxen im Edit-Modus
  roleChecked(r: Role) { return this.editRoles.includes(r); }
  // Rolle im Edit-Modus togglen
  toggleRole(r: Role) {
    this.editRoles = this.editRoles.includes(r)
      ? this.editRoles.filter(x => x !== r)
      : [...this.editRoles, r];
  }

  // role labels provided by RoleLabelPipe

  // Für *ngFor Performance
  trackById(_: number, u: AdminUser) { return u.id; }
}


