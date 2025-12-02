// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { AppNotification } from '../../../../core/notifications/notification.model';
import { FormsModule } from '@angular/forms';

export type ProjectOption = { label: string; value: number };
export type SimpleStudent = { name: string; class: string; avatarUrl?: string };

@Component({
  standalone: true,
  selector: 'teacher-header-bar',
  imports: [CommonModule],
  templateUrl: './header-bar.html',
  styleUrl: './header-bar.scss',
})
export class TeacherHeaderBarComponent {
  // Der Schüler, der oben in der Leiste angezeigt wird
  @Input() student!: SimpleStudent;
  // Klick auf den Button: neues Projekt anlegen (Event nach außen)
  @Output() createProject = new EventEmitter<void>();

  imgFailed = false;
  // Falls das Bild kaputt ist, zeige ich den Initialen-Kreis
  markImgFailed() {
    this.imgFailed = true;
  }

  // Optional notification count (for bell badge)
  @Input() notifications = 0;

  constructor(private readonly notificationsSvc: NotificationService) {}

  showPanel = false;
  get items$(): Observable<AppNotification[]> {
    return this.notificationsSvc.items$;
  }
  get unreadCount$(): Observable<number> {
    return this.notificationsSvc.unreadCount$;
  }

  // Öffnet/Schließt das kleine Benachrichtigungsfenster
  togglePanel() {
    this.showPanel = !this.showPanel;
  }
  closePanel() {
    this.showPanel = false;
  }

  trackById(_: number, item: AppNotification) {
    return item.id;
  }

  // Markiert alle Benachrichtigungen als gelesen
  markAllRead() {
    this.notificationsSvc.markAllRead();
  }
}
