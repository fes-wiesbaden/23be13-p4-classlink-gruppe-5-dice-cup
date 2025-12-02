// Von Lukas bearbeitet
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';

export type AdminKpis = { total: number; active: number; admins: number };

@Component({
  standalone: true,
  selector: 'admin-kpi-cards',
  imports: [CommonModule, Card],
  templateUrl: './kpi-cards.html',
  styleUrl: './kpi-cards.scss',
})
export class AdminKpiCardsComponent {
  // Zeigt kleine Kennzahlen oben (Gesamt/Aktiv/Admins)
  @Input() kpis!: AdminKpis;
}
