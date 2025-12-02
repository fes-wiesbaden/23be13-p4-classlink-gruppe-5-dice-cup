// Von Lukas bearbeitet
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';

export interface Scores {
  teacher: string;
  peer: string;
  self: string;
  trendTeacher: string;
  trendPeer: string;
  trendSelf: string;
}

@Component({
  standalone: true,
  selector: 'teacher-kpi-cards',
  imports: [CommonModule, Card],
  templateUrl: './kpi-cards.html',
  styleUrl: './kpi-cards.scss',
})
export class TeacherKpiCardsComponent {
  // Die drei Noten-Werte, die ich anzeige (Lehrer/Peer/Selbst)
  @Input() scores!: Scores;
  // Optional, damit der Lehrer seine Note direkt anpassen könnte
  @Input() allowTeacherEdit = false;

  private toNumber(val: string): number {
    // Supports "1.7" or "1,7" styles just in case
    return Number(String(val).replace(',', '.'));
  }

  // Returns 'better' if value < teacher (German grading: lower is better)
  // Einfache Einordnung, ob die Note besser/schlechter/gleich ist
  deltaClassFor(value: string, teacher: string): 'better' | 'worse' | 'equal' {
    const v = this.toNumber(value);
    const t = this.toNumber(teacher);
    if (isNaN(v) || isNaN(t)) return 'equal';
    if (v < t) return 'better';
    if (v > t) return 'worse';
    return 'equal';
  }
}
