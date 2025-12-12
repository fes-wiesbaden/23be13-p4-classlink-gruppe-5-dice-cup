// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Tooltip } from 'primeng/tooltip';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';

export interface TeacherStudent {
  id: number;
  name: string;
  class: string;
  avatarUrl?: string;
}

@Component({
  standalone: true,
  selector: 'teacher-student-sidebar',
  imports: [CommonModule, FormsModule, InputText, Tooltip, SelectModule, ButtonModule],
  templateUrl: './student-sidebar.html',
  styleUrl: './student-sidebar.scss',
})
export class TeacherStudentSidebarComponent {
  @Input() students: TeacherStudent[] = [];
  @Input() selectedStudentId: number | null = null;
  @Input() search = '';
  @Input() classes: string[] = [];
  @Input() selectedClass: string | null = null;
  @Input() teacherName = '';
  @Input() teacherSubject = '';
  @Input() teacherRole = '';

  @Input() assignClassName: string | null = null;
  @Output() selectStudent = new EventEmitter<number>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() classChange = new EventEmitter<string | null>();
  @Output() createClass = new EventEmitter<void>();
  @Output() assignClassChange = new EventEmitter<string | null>();
  @Output() assignSelected = new EventEmitter<void>();

  
  trackById(_: number, item: TeacherStudent) {
    return item.id;
  }

  get classOptions() {
    return this.classes.map((c) => ({ label: c, value: c }));
  }

  get assignOptions() {
    return this.classes.map((c) => ({ label: c, value: c }));
  }

  
  private failed = new Set<number>();
  
  isFailed(id: number) {
    return this.failed.has(id);
  }
  
  markFailed(id: number) {
    this.failed.add(id);
  }
}
