// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';

export interface TeacherStudent {
  id: number;
  name: string;
  class: string;
  avatarUrl?: string;
}

@Component({
  standalone: true,
  selector: 'teacher-project-assignment',
  imports: [CommonModule, FormsModule, Card, Select],
  templateUrl: './project-assignment.html',
  styleUrl: './project-assignment.scss',
})
export class TeacherProjectAssignmentComponent {
  // Hier kommen Name und Auswahl der Projekte rein
  @Input() projectName = '';
  @Input() students: TeacherStudent[] = [];
  // Diese IDs sind aktuell dem Projekt zugeordnet
  @Input() assignedIds: number[] = [];
  @Input() projectOptions: { label: string; value: number }[] = [];
  @Input() projectId!: number;
  // Wenn ich das Projekt im Select ändere
  @Output() projectChange = new EventEmitter<number>();
  // Klick auf den Toggle pro Schüler
  @Output() studentToggle = new EventEmitter<number>();

  // Hilfsfunktionen für Anzeige und Performance
  isAssigned(id: number) {
    return this.assignedIds.includes(id);
  }
  trackById(_: number, item: TeacherStudent) {
    return item.id;
  }

  // Fallback when an avatar image fails to load
  private failed = new Set<number>();
  isFailed(id: number) {
    return this.failed.has(id);
  }
  markFailed(id: number) {
    this.failed.add(id);
  }
}
