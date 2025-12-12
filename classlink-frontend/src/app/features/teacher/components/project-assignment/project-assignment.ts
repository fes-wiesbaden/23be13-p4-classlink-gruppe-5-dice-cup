// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';

export interface TeacherStudent {
  id: string;
  name: string;
  className: string;
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
  @Input() assignedIds: string[] = [];
  @Input() projectOptions: { label: string; value: string }[] = [];
  @Input() projectId!: string;
  // Wenn ich das Projekt im Select ändere
  @Output() projectChange = new EventEmitter<string>();
  // Klick auf den Toggle pro Schüler
  @Output() studentToggle = new EventEmitter<string>();
  // Neues Projekt anlegen
  @Output() createProject = new EventEmitter<void>();

  // Hilfsfunktionen für Anzeige und Performance
  isAssigned(id: string) {
    return this.assignedIds.includes(id);
  }
  trackById(_: number, item: TeacherStudent) {
    return item.id;
  }

  // Fallback when an avatar image fails to load
  private failed = new Set<string>();
  isFailed(id: string) {
    return this.failed.has(id);
  }
  markFailed(id: string) {
    this.failed.add(id);
  }
}
