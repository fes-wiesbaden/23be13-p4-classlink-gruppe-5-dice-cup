// Von Lukas bearbeitet
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'grade-history-strip',
  imports: [CommonModule],
  templateUrl: './grade-history.html',
  styleUrl: './grade-history.scss',
})
export class GradeHistoryStripComponent {
  @Input() labels: string[] = [];
  @Input() teacher: number[] = [];
  @Input() peer: number[] = [];
  @Input() self: number[] = [];

  // Für *ngFor, damit Angular weniger neu rendert
  trackIndex(index: number) { return index; }

  // Farbskala: je besser die Note, desto grüner â€” greener is better
  gradeColor(grade: number): string {
    const g = Number(grade);
    if (isNaN(g)) return 'transparent';
    if (g <= 1.5) return 'var(--grade-best, #22c55e)';     // sehr gut
    if (g <= 2.5) return 'var(--grade-good, #4ade80)';     // gut
    if (g <= 3.5) return 'var(--grade-ok, #f59e0b)';       // befriedigend
    if (g <= 4.5) return 'var(--grade-weak, #f97316)';     // ausreichend
    return 'var(--grade-bad, #ef4444)';                    // mangelhaft/ungenÃ¼gend
  }
}


