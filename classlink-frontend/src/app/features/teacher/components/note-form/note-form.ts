// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Card } from 'primeng/card';

@Component({
  standalone: true,
  selector: 'teacher-note-form',
  imports: [CommonModule, FormsModule, Card],
  templateUrl: './note-form.html',
  styleUrl: './note-form.scss',
})
export class TeacherNoteFormComponent {
  // Name nur für die Anzeige im Formular
  @Input() studentName = '';
  // Zweibindung für das Textfeld
  @Input() text = '';
  @Output() textChange = new EventEmitter<string>();
  // Beim Klick auf Senden feuere ich dieses Event
  @Output() send = new EventEmitter<void>();
}
