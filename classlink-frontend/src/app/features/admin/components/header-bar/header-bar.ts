// Von Lukas bearbeitet
import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'admin-header-bar',
  imports: [CommonModule, FormsModule],
  templateUrl: './header-bar.html',
  styleUrl: './header-bar.scss',
})
export class AdminHeaderBarComponent {
  @Output() createUser = new EventEmitter<{ name: string; email: string; roles: ('student'|'teacher'|'admin')[] }>();
  @Input() busy = false;

  // simple internal state for quick create
  showCreate = false;
  name = '';
  email = '';
  roles: ('student'|'teacher'|'admin')[] = ['student'];

  // Öffnet/schließt das kleine Formular oben
  toggleCreate() { this.showCreate = !this.showCreate; }

  // Legt den Benutzer an, wenn Name & E-Mail gefüllt sind
  add() {
    const name = this.name.trim();
    const email = this.email.trim();
    if (!name || !email) return;
    this.createUser.emit({ name, email, roles: [...this.roles] });
    this.name = '';
    this.email = '';
    this.roles = ['student'];
    this.showCreate = false;
  }

  // Für die Checkboxen im Formular
  roleChecked(r: 'student'|'teacher'|'admin') { return this.roles.includes(r); }
  // Rolle an/aus schalten
  toggleRole(r: 'student'|'teacher'|'admin') {
    this.roles = this.roles.includes(r)
      ? this.roles.filter(x => x !== r)
      : [...this.roles, r];
  }
}


