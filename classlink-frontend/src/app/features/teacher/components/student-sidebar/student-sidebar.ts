// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ClassOption } from '../../models';

export interface TeacherStudent {
  id: string;
  name: string;
  className: string;
  avatarUrl?: string;
}

@Component({
  standalone: true,
  selector: 'teacher-student-sidebar',
  imports: [CommonModule, FormsModule, InputText, SelectModule, ButtonModule],
  templateUrl: './student-sidebar.html',
  styleUrl: './student-sidebar.scss',
})
export class TeacherStudentSidebarComponent {
  @Input() students: TeacherStudent[] = [];
  @Input() selectedStudentId: string | null = null;
  @Input() search = '';
  @Input() classes: ClassOption[] = [];
  @Input() selectedClass: string | null = null;
  @Input() teacherName = '';
  @Input() teacherSubject = '';
  @Input() teacherRole = '';
  // For assigning the currently selected student to a class
  @Input() assignClassName: string | null = null;
  @Output() selectStudent = new EventEmitter<string>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() classChange = new EventEmitter<string | null>();
  @Output() createClass = new EventEmitter<void>();
  @Output() assignClassChange = new EventEmitter<string | null>();
  @Output() assignSelected = new EventEmitter<void>();

  // Das brauche ich für ngFor, damit Angular effizienter rendert
  trackById(_: number, item: TeacherStudent) {
    return item.id;
  }

  get classOptions() {
    return this.classes.map((c) => ({ label: c.name, value: c.id }));
  }

  get assignOptions() {
    return this.classes.map((c) => ({ label: c.name, value: c.id }));
  }

  // Fallback when an avatar image fails to load
  private failed = new Set<string>();
  // Wenn ein Bild nicht lädt, merke ich mir die ID
  isFailed(id: string) {
    return this.failed.has(id);
  }
  // Dann zeige ich stattdessen den Anfangsbuchstaben an
  markFailed(id: string) {
    this.failed.add(id);
  }
}
