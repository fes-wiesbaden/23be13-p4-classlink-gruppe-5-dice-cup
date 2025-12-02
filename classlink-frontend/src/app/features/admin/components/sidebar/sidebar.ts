// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';

@Component({
  standalone: true,
  selector: 'admin-sidebar',
  imports: [CommonModule, FormsModule, InputText],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class AdminSidebarComponent {
  // Einfache Suchleiste links, gibt Text nach außen

  @Input() search = '';
  @Output() searchChange = new EventEmitter<string>(); // ändert sich bei Eingabe
}
